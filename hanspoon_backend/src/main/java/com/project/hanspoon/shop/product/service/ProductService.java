package com.project.hanspoon.shop.product.service;

import com.project.hanspoon.shop.constant.ProductSort;
import com.project.hanspoon.shop.product.dto.*;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.product.entity.ProductImage;
import com.project.hanspoon.shop.product.entity.ProductImageType;
import com.project.hanspoon.shop.mapper.ProductMapper;
import com.project.hanspoon.shop.product.repository.ProductImageRepository;
import com.project.hanspoon.shop.product.repository.ProductRepository;

import com.project.hanspoon.shop.product.repository.spec.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * ProductService
 * - 상품 도메인의 “업무 로직”을 담당하는 서비스 계층
 * - 조회(list/detail)는 readOnly 트랜잭션으로 최적화
 * - 등록/수정(create/update)은 메서드 단위로 @Transactional을 붙여 쓰기 트랜잭션으로 오버라이드
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 클래스 레벨 기본을 readOnly=true로 둬서
// 조회성 메서드가 기본적으로 flush 부담 없이 동작(성능/안전성 개선)
// 쓰기 메서드는 메서드에 @Transactional을 다시 붙여 readOnly=false로 전환
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductImageService productImageService;

    /**
     * ✅ 상품 목록 조회
     * - 검색 조건(ProductSearchRequest) + 페이징/정렬 + 대표 썸네일 포함
     *
     * 핵심 포인트:
     * 1) Specification으로 검색 조건을 동적으로 결합
     * 2) Product 페이지 조회 1번
     * 3) 해당 페이지의 상품 id 목록으로 대표 이미지(repYn=true)를 IN 쿼리 1번 조회
     * → 목록 페이지당 “쿼리 2방”으로 끝낼 수 있어 N+1 방지에 유리
     */
    public Page<ProductListResponseDto> list(ProductSearchRequest search, int page, int size, ProductSort sortType) {

        // page는 0 미만 방지 (스프링 PageRequest는 0-based 페이지 인덱스)
        int safePage = Math.max(page, 0);

        // size는 1~100 범위로 강제 (과도한 요청으로 인한 부하 방지)
        int safeSize = Math.min(Math.max(size, 1), 100);

        // 정렬 기준 구성
        // - PRICE_ASC: price 오름차순, 같은 price면 id 내림차순(안정적인 정렬을 위한 tie-breaker)
        // - PRICE_DESC: price 내림차순, 같은 price면 id 내림차순
        // - default: id 내림차순(최신 등록 순)
        Sort sort = switch (sortType) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            default -> Sort.by(Sort.Direction.DESC, "id");
        };

        // PageRequest 생성 (page, size, sort)
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        // Specification 초기화
        // where(null) 대신 항상 true 조건(conjunction)으로 시작해 경고를 제거한다.
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        // 검색 조건이 들어온 경우에만 조건을 하나씩 결합
        if (search != null) {

            // 카테고리 필터: category == ?
            if (search.getCategory() != null) {
                spec = spec.and(ProductSpecifications.categoryEq(search.getCategory()));
            }

            // 키워드 필터: name LIKE %keyword%
            // trim() + isEmpty 체크로 공백만 들어오는 경우 제외
            if (search.getKeyword() != null && !search.getKeyword().trim().isEmpty()) {
                spec = spec.and(ProductSpecifications.nameContains(search.getKeyword().trim()));
            }

            // 최소 가격: price >= minPrice
            if (search.getMinPrice() != null) {
                spec = spec.and(ProductSpecifications.priceGte(search.getMinPrice()));
            }

            // 최대 가격: price <= maxPrice
            if (search.getMaxPrice() != null) {
                spec = spec.and(ProductSpecifications.priceLte(search.getMaxPrice()));
            }
        }

        // Product 페이지 조회
        // - spec + pageable 적용
        // - 결과: 해당 페이지의 content(상품 리스트) + total count 포함
        Page<Product> result = productRepository.findAll(spec, pageable);

        // 현재 페이지에 포함된 상품들의 id만 추출 (대표 이미지 IN 조회에 사용)
        List<Long> ids = result.getContent().stream()
                .map(Product::getId)
                .toList();

        // productId -> thumbnailUrl(대표이미지 url) 매핑을 위한 맵
        Map<Long, String> thumbMap = new HashMap<>();

        // 현재 페이지에 상품이 있을 때만 대표 이미지들을 한 번에 조회
        if (!ids.isEmpty()) {

            // repYn=true 인 대표 이미지들만 IN 쿼리로 조회
            List<ProductImage> reps = productImageRepository.findByProduct_IdInAndRepYnTrue(ids);

            // 조회한 대표 이미지들을 productId 기준으로 맵에 담기
            for (ProductImage img : reps) {
                // img.getProduct().getId()를 통해 어느 상품의 대표이미지인지 키를 만든다.
                // (JPA 프록시의 id 접근은 보통 추가 쿼리 없이 가능하지만, 환경에 따라 주의 포인트가 될 수 있음 - 리뷰 참고)
                thumbMap.put(img.getProduct().getId(), img.getImgUrl());
            }
        }

        // Product 엔티티 -> ProductListResponseDto 변환
        // - thumbMap에서 해당 상품의 대표 썸네일 url을 찾아 넣어준다.
        List<ProductListResponseDto> dtos = result.getContent().stream()
                .map(p -> ProductMapper.toListDto(p, thumbMap.get(p.getId())))
                .toList();

        // Page<Product>를 그대로 쓰지 않고,
        // DTO 리스트로 새 PageImpl을 만들어 반환
        // - totalElements는 원본 result에서 가져와 페이징 정보 유지
        return new PageImpl<>(dtos, pageable, result.getTotalElements());
    }

    /**
     * ✅ 상품 상세 조회
     * - 상품 1건 + 이미지 리스트 포함
     *
     * 흐름:
     * 1) productRepository.findById로 상품 조회 (없으면 404)
     * 2) productImageService.list(id)로 이미지 DTO 리스트 조회
     * 3) repYn=true 이미지를 찾아 thumbnailUrl 구성
     * 4) ProductMapper로 최종 상세 응답 DTO 조립
     */
    public ProductDetailResponseDto getDetail(Long id) {

        // 상품 조회 (없으면 404 응답으로 처리)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + id));

        // 해당 상품의 이미지 목록을 DTO 형태로 가져옴
        List<ProductImageResponseDto> images = productImageService.list(id, ProductImageType.MAIN);
        List<ProductImageResponseDto> detailImages = productImageService.list(id, ProductImageType.DETAIL);

        // 대표 이미지(repYn=true)의 imgUrl을 찾아 thumbnailUrl로 사용
        // 대표가 없으면 null (프론트에서 placeholder 처리 등)
        String thumbnailUrl = images.stream()
                .filter(ProductImageResponseDto::isRepYn)
                .map(ProductImageResponseDto::getImgUrl)
                .findFirst()
                .orElse(null);

        // 상품 엔티티 + thumbnailUrl + images 를 상세 DTO로 조립
        return ProductMapper.toDetailDto(product, thumbnailUrl, images, detailImages);
    }

    /**
     * ✅ 상품 등록 (JSON만 등록, 이미지는 별도)
     * - ProductRequestDto -> Product 엔티티 저장
     * - 저장 후 getDetail로 “상세 응답” 형태로 반환
     *
     * 참고:
     * - create는 저장 후 바로 getDetail을 호출하므로 조회 쿼리가 추가로 1번 더 발생함(리뷰 참고)
     */
    @Transactional
    public ProductDetailResponseDto create(ProductRequestDto dto) {

        // DTO -> Entity 변환 후 저장
        Product saved = productRepository.save(ProductMapper.toEntity(dto));

        // 저장된 id로 상세 조회(이미지 포함 형태의 응답 반환)
        return getDetail(saved.getId());
    }

    /**
     * ✅ 상품 등록 + 이미지 업로드를 한 번에 처리
     * - “이미지는 최소 1장 필수” 규칙을 서비스에서 강제
     * - 상품 저장 후, productImageService.upload로 이미지 업로드/DB 저장
     * - 마지막에 getDetail로 상세 응답 반환
     */
    @Transactional
    public ProductDetailResponseDto createWithImages(ProductRequestDto dto,
                                                     List<MultipartFile> files,
                                                     Integer repIndex) {

        // 파일 리스트 중 “실제로 비어있지 않은 파일”이 1개라도 있는지 체크
        boolean hasAtLeastOneFile = files != null
                && files.stream().anyMatch(f -> f != null && !f.isEmpty());

        // 이미지 0장 등록 방지
        if (!hasAtLeastOneFile) {
            throw new IllegalArgumentException("상품 이미지는 최소 1장 이상 필수입니다.");
        }

        // 상품 먼저 저장 (product_id 확보)
        Product saved = productRepository.save(ProductMapper.toEntity(dto));

        // 이미지 업로드/저장 처리
        // repIndex는 대표 이미지 선택(예: 0번 파일이 대표) 같은 정책에 사용될 가능성이 큼
        productImageService.upload(saved.getId(), files, repIndex, ProductImageType.MAIN);

        // 최종 상세 응답 반환 (상품 + 이미지 포함)
        return getDetail(saved.getId());
    }

    /**
     * ✅ 상품 수정
     * - 대상 상품 조회 (없으면 404)
     * - DTO 값을 엔티티에 반영(ProductMapper.applyToEntity)
     * - 저장 후 상세 응답 반환
     */
    @Transactional
    public ProductDetailResponseDto update(Long id, ProductRequestDto dto) {

        // 수정 대상 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + id));

        // DTO -> 기존 엔티티에 값 반영 (name/price/stock/category 등)
        ProductMapper.applyToEntity(dto, product);

        // 저장 호출
        // - 조회한 엔티티는 영속 상태(managed)라면 save 없이도 커밋 시점에 dirty checking으로 반영됨
        // - 하지만 명시적으로 save를 호출해도 기능상 문제는 없음(리뷰 참고)
        productRepository.save(product);

        // 수정 후 상세 응답 반환
        return getDetail(id);
    }
}
