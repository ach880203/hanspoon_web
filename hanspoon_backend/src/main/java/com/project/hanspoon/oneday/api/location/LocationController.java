package com.project.hanspoon.oneday.api.location;

import com.project.hanspoon.oneday.api.location.dto.GeocodeResponseDTO;
import org.springframework.web.bind.annotation.*;

/**
 * 원데이 위치(지도) 관련 API
 */
@RestController
@RequestMapping("/api/oneday/location")
public class LocationController {

    private final KakaoLocationService kakaoLocationService;

    public LocationController(KakaoLocationService kakaoLocationService) {
        this.kakaoLocationService = kakaoLocationService;
    }

    /**
     * 주소 -> 좌표 변환
     * 예) GET /api/oneday/location/geocode?query=서울 중구 세종대로 110
     */
    @GetMapping("/geocode")
    public GeocodeResponseDTO geocode(@RequestParam("query") String query) {
        return kakaoLocationService.geocode(query);
    }
}
