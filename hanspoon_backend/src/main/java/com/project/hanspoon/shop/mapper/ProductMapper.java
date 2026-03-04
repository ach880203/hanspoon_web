package com.project.hanspoon.shop.mapper;

import com.project.hanspoon.shop.product.dto.*;
import com.project.hanspoon.shop.product.entity.Product;

import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
        // util class
    }

    /**
     * ✅ 등록(Create): DTO -> 새 Entity
     */
    public static Product toEntity(ProductRequestDto dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setDetailContent(dto.getDetailContent());
        return product;
    }

    /**
     * ✅ 수정(Update): DTO -> 기존 영속 엔티티에 반영
     * - 서비스에서 findById로 가져온 entity에 적용해야 안전함
     */
    public static void applyToEntity(ProductRequestDto dto, Product entity) {
        if (dto == null) throw new IllegalArgumentException("상품 요청 데이터가 비어 있습니다.");
        if (entity == null) throw new IllegalArgumentException("상품 엔티티가 비어 있습니다.");

        entity.setCategory(dto.getCategory());
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setDetailContent(dto.getDetailContent());
    }

    /**
     * ✅ 응답(Response): Entity -> DTO
     */
    public static ProductResponseDto toResponseDto(Product entity) {
        if (entity == null) return null;

        return ProductResponseDto.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .name(entity.getName())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .build();
    }

    public static ProductListResponseDto toListDto(Product p, String thumbnailUrl) {
        return ProductListResponseDto.builder()
                .id(p.getId())
                .category(p.getCategory())
                .name(p.getName())
                .price(p.getPrice())
                .stock(p.getStock())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public static ProductDetailResponseDto toDetailDto(
            Product p,
            String thumbnailUrl,
            List<ProductImageResponseDto> images,
            List<ProductImageResponseDto> detailImages
    ) {
        return ProductDetailResponseDto.builder()
                .id(p.getId())
                .category(p.getCategory())
                .name(p.getName())
                .price(p.getPrice())
                .stock(p.getStock())
                .detailContent(p.getDetailContent())
                .thumbnailUrl(thumbnailUrl)
                .images(images)
                .detailImages(detailImages)
                .build();
    }
}
