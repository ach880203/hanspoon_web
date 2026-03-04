package com.project.hanspoon.shop.product.service;

import com.project.hanspoon.shop.product.dto.ProductImageResponseDto;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.product.entity.ProductImage;
import com.project.hanspoon.shop.product.entity.ProductImageType;
import com.project.hanspoon.shop.product.repository.ProductImageRepository;
import com.project.hanspoon.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    @Transactional
    public List<ProductImageResponseDto> upload(
            Long productId,
            List<MultipartFile> files,
            Integer repIndex,
            ProductImageType imageType
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId));

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        ProductImageType targetType = imageType == null ? ProductImageType.MAIN : imageType;
        int rep = (repIndex == null) ? 0 : repIndex;
        if (rep < 0 || rep >= files.size()) rep = 0;

        if (targetType == ProductImageType.MAIN) {
            productImageRepository.findByProduct_IdOrderByRepYnDescIdAsc(productId).stream()
                    .filter(this::isMainImage)
                    .filter(ProductImage::isRepYn)
                    .findFirst()
                    .ifPresent(img -> img.setRepYn(false));
        }

        ensureDirExists(itemImgLocation);

        List<ProductImageResponseDto> result = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty()) continue;

            String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String ext = getExtension(originalName);
            String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path savePath = Paths.get(itemImgLocation).resolve(storedName);

            try {
                file.transferTo(savePath);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + originalName, e);
            }

            String imgUrl = "/images/" + storedName;

            ProductImage saved = productImageRepository.save(
                    ProductImage.builder()
                            .product(product)
                            .originalName(originalName)
                            .storedName(storedName)
                            .imgUrl(imgUrl)
                            .repYn(targetType == ProductImageType.MAIN && i == rep)
                            .imageType(targetType)
                            .build()
            );

            result.add(toDto(saved));
        }

        return result;
    }

    public List<ProductImageResponseDto> list(Long productId, ProductImageType imageType) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId));

        ProductImageType targetType = imageType == null ? ProductImageType.MAIN : imageType;
        return productImageRepository.findByProduct_IdOrderByRepYnDescIdAsc(productId)
                .stream()
                .filter(img -> isType(img, targetType))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "이미지가 없습니다. id=" + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("해당 상품의 이미지가 아닙니다.");
        }

        Path path = Paths.get(itemImgLocation).resolve(image.getStoredName());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + image.getStoredName(), e);
        }

        boolean wasRep = image.isRepYn();
        productImageRepository.delete(image);

        if (wasRep && isMainImage(image)) {
            List<ProductImage> remain = productImageRepository.findByProduct_IdOrderByRepYnDescIdAsc(productId);
            remain.stream()
                    .filter(this::isMainImage)
                    .findFirst()
                    .ifPresent(next -> next.setRepYn(true));
        }
    }

    private ProductImageResponseDto toDto(ProductImage img) {
        ProductImageType type = img.getImageType() == null ? ProductImageType.MAIN : img.getImageType();
        return ProductImageResponseDto.builder()
                .id(img.getId())
                .originalName(img.getOriginalName())
                .imgUrl(img.getImgUrl())
                .repYn(img.isRepYn())
                .imageType(type)
                .build();
    }

    private boolean isMainImage(ProductImage img) {
        return img.getImageType() == null || img.getImageType() == ProductImageType.MAIN;
    }

    private boolean isType(ProductImage img, ProductImageType imageType) {
        if (imageType == ProductImageType.DETAIL) {
            return img.getImageType() == ProductImageType.DETAIL;
        }
        return isMainImage(img);
    }

    private void ensureDirExists(String dir) {
        try {
            Files.createDirectories(Paths.get(dir));
        } catch (IOException e) {
            throw new RuntimeException("디렉토리 생성 실패: " + dir, e);
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1);
    }
}
