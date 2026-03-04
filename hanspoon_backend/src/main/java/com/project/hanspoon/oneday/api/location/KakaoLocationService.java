package com.project.hanspoon.oneday.api.location;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.api.location.dto.GeocodeResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 카카오 로컬 API 호출 서비스
 * - REST 키는 반드시 서버에서만 관리(프론트 노출 금지)
 */
@Service
public class KakaoLocationService {
    private static final String ADDRESS_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/address.json";
    private static final String KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    @Value("${kakao.rest-api-key:}")
    private String kakaoRestApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 검색어 -> 좌표 변환(지오코딩)
     * 1) 주소 검색 API 우선
     * 2) 결과가 없으면 키워드(건물명/장소명) 검색 API로 재시도
     *
     * @param query 사용자가 입력한 주소/건물명 문자열
     * @return address, lat, lng
     */
    public GeocodeResponseDTO geocode(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            throw new BusinessException("주소를 입력해 주세요.");
        }

        // REST API 키는 지도 JavaScript 키와 다릅니다.
        // 키 누락/오입력 시 앱 전체 기동은 유지하고, 주소검색 시점에 원인을 안내합니다.
        String restApiKey = kakaoRestApiKey == null ? "" : kakaoRestApiKey.trim();
        if (restApiKey.isEmpty()) {
            throw new BusinessException("카카오 주소검색 REST API 키가 설정되지 않았습니다. 백엔드 KAKAO_REST_API_KEY를 확인해 주세요.");
        }

        List<Map> addressDocuments = requestDocuments(ADDRESS_SEARCH_URL, normalizedQuery, restApiKey);
        if (!addressDocuments.isEmpty()) {
            return toAddressResponse(addressDocuments.get(0));
        }

        // 주소 검색 결과가 없을 때 건물명/상호명 검색으로 보완합니다.
        List<Map> keywordDocuments = requestDocuments(KEYWORD_SEARCH_URL, normalizedQuery, restApiKey);
        if (!keywordDocuments.isEmpty()) {
            return toKeywordResponse(keywordDocuments.get(0));
        }

        throw new BusinessException("검색 결과가 없습니다. 주소나 건물명을 다시 확인해 주세요.");
    }

    private List<Map> requestDocuments(String endpoint, String query, String restApiKey) {
        String url = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("query", query)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new BusinessException("카카오 주소검색 인증에 실패했습니다. REST API 키와 카카오 개발자 콘솔 설정을 확인해 주세요.");
        } catch (RestClientException e) {
            throw new BusinessException("카카오 주소검색 호출에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }

        Map body = response.getBody();
        if (body == null) {
            return List.of();
        }
        Object documents = body.get("documents");
        if (!(documents instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .toList();
    }

    private GeocodeResponseDTO toAddressResponse(Map first) {
        // 주소 검색 응답 기준: x=경도, y=위도
        String addressName = normalizeText(first.get("address_name"));
        double lng = parseCoordinate(first.get("x"), "경도");
        double lat = parseCoordinate(first.get("y"), "위도");
        return new GeocodeResponseDTO(addressName, lat, lng);
    }

    private GeocodeResponseDTO toKeywordResponse(Map first) {
        // 건물명 검색 응답에서 도로명 주소 우선, 없으면 지번 주소/장소명 순으로 fallback
        String roadAddressName = normalizeText(first.get("road_address_name"));
        String jibunAddressName = normalizeText(first.get("address_name"));
        String placeName = normalizeText(first.get("place_name"));

        String resolvedAddress = !roadAddressName.isBlank()
                ? roadAddressName
                : !jibunAddressName.isBlank()
                        ? jibunAddressName
                        : placeName;

        double lng = parseCoordinate(first.get("x"), "경도");
        double lat = parseCoordinate(first.get("y"), "위도");
        return new GeocodeResponseDTO(resolvedAddress, lat, lng);
    }

    private String normalizeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private double parseCoordinate(Object value, String axisLabel) {
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            throw new BusinessException("카카오 응답에서 " + axisLabel + " 좌표 파싱에 실패했습니다.");
        }
    }
}
