package com.project.hanspoon.shop.product.controller;

import com.project.hanspoon.shop.constant.ProductSort;
import com.project.hanspoon.shop.product.dto.ProductDetailResponseDto;
import com.project.hanspoon.shop.product.dto.ProductListResponseDto;
import com.project.hanspoon.shop.product.dto.ProductRequestDto;
import com.project.hanspoon.shop.product.dto.ProductSearchRequest;
import com.project.hanspoon.shop.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * ProductController
 * - /api/products 하위에서 상품 CRUD + 검색/정렬/페이징 API를 제공하는 REST 컨트롤러
 *
 * 역할:
 * - HTTP 요청을 받아 DTO로 바인딩(@RequestBody/@ModelAttribute/@RequestPart)
 * - 입력값 검증(@Valid)
 * - 서비스 호출
 * - ResponseEntity로 상태코드/헤더/바디를 구성해 반환
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * ✅ 상품 목록 조회 (페이지 + 검색 + 정렬)
     *
     * 요청 예:
     * GET /api/products?category=INGREDIENT&keyword=양파&minPrice=1000&maxPrice=5000&page=0&size=20&sort=LATEST
     *
     * 바인딩:
     * - search: @ModelAttribute로 query parameter를 ProductSearchRequest에 자동 바인딩
     * - page/size/sort: @RequestParam으로 개별 파라미터 바인딩 + 기본값 지정
     *
     * 응답:
     * - Page<ProductListResponseDto>로 페이징 정보(content, totalElements, totalPages 등) 포함
     */
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> list(
            @ModelAttribute ProductSearchRequest search,           // query string 필터 조건
            @RequestParam(defaultValue = "0") int page,             // 0-based page index
            @RequestParam(defaultValue = "20") int size,            // 페이지 크기
            @RequestParam(defaultValue = "LATEST") ProductSort sort // 정렬 타입(enum)
    ) {
        return ResponseEntity.ok(productService.list(search, page, size, sort));
    }

    /**
     * ✅ 상품 상세 조회 (이미지 포함)
     *
     * 요청 예:
     * GET /api/products/{id}
     *
     * 바인딩:
     * - @PathVariable로 URL 경로 변수 id를 Long으로 받음
     *
     * 응답:
     * - ProductDetailResponseDto(상품 + thumbnailUrl + images)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponseDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getDetail(id));
    }

    /**
     * ✅ 상품 등록 (JSON)
     *
     * 요청 예:
     * POST /api/products
     * Content-Type: application/json
     *
     * 바인딩:
     * - @RequestBody로 JSON을 ProductRequestDto로 역직렬화
     * - @Valid로 DTO에 선언된 validation(@NotNull/@NotBlank/@Min 등) 검증 수행
     *
     * 응답:
     * - 201 Created
     * - Location 헤더: /api/products/{id}
     * - Body: 생성된 상품 상세 정보(현재는 ProductDetailResponseDto를 반환)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDetailResponseDto> createJson(@RequestBody @Valid ProductRequestDto dto) {
        ProductDetailResponseDto created = productService.create(dto);

        // 201 + Location 헤더 설정
        return ResponseEntity.created(URI.create("/api/products/" + created.getId()))
                .body(created);
    }

    /**
     * ✅ 상품 등록 + 이미지 업로드 (multipart/form-data)
     *
     * form-data 구성:
     * - product: JSON (application/json)
     * - files: 이미지 파일들(multipart)
     * - repIndex: 대표 이미지 인덱스(옵션)
     *
     * 바인딩:
     * - @RequestPart("product"): 멀티파트 파트 중 product 파트를 JSON으로 파싱해 ProductRequestDto로 바인딩
     * - @RequestPart("files"): 멀티파트 파일 파트들을 List<MultipartFile>로 받음
     * - @RequestParam repIndex: 쿼리스트링 또는 form-field로 받는 단일 값
     *
     * 응답:
     * - 201 Created + Location
     * - Body: 생성된 상품 상세 정보
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDetailResponseDto> createMultipart(
            @RequestPart("product") @Valid ProductRequestDto dto,
            @RequestPart("files") List<MultipartFile> files,   // required=true 기본값 (없으면 400)
            @RequestParam Integer repIndex                      // 현재는 필수(옵션이라면 required=false 필요)
    ) {
        // 서비스에서도 검증하긴 하지만(이미지 1장 이상), 컨트롤러에서 1차 방어
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("이미지는 최소 1장 이상 필수입니다.");
        }

        ProductDetailResponseDto created = productService.createWithImages(dto, files, repIndex);

        return ResponseEntity.created(URI.create("/api/products/" + created.getId()))
                .body(created);
    }

    /**
     * ✅ 상품 수정 (JSON)
     *
     * 요청 예:
     * PUT /api/products/{id}
     * Content-Type: application/json
     *
     * 바인딩:
     * - 경로 id는 @PathVariable
     * - 수정 값은 @RequestBody + @Valid DTO 검증
     *
     * 응답:
     * - 200 OK + 수정된 상품 상세 정보
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDetailResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDto dto
    ) {
        return ResponseEntity.ok(productService.update(id, dto));
    }
}