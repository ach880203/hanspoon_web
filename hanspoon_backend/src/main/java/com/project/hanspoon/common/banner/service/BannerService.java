package com.project.hanspoon.common.banner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hanspoon.common.banner.dto.BannerBadgeDto;
import com.project.hanspoon.common.banner.dto.BannerDto;
import com.project.hanspoon.common.banner.entity.Banner;
import com.project.hanspoon.common.banner.repository.BannerRepository;
import com.project.hanspoon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private static final TypeReference<List<BannerBadgeDto>> BADGE_LIST_TYPE = new TypeReference<>() {
    };

    private final BannerRepository bannerRepository;
    private final ObjectMapper objectMapper;
    @Value("${itemImgLocation}")
    private String itemImgLocation;

    public List<BannerDto> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortOrderAscBannerIdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<BannerDto> getAllBannersForAdmin() {
        return bannerRepository.findAllByOrderBySortOrderAscBannerIdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public BannerDto getBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 배너입니다."));
        return toDto(banner);
    }

    @Transactional
    public BannerDto createBanner(BannerDto dto) {
        validate(dto);
        Banner created = bannerRepository.save(
                Banner.builder()
                        .eyebrow(trimOrNull(dto.getEyebrow()))
                        .title(dto.getTitle().trim())
                        .period(trimOrNull(dto.getPeriod()))
                        .imageSrc(dto.getImageSrc().trim())
                        .imageAlt(trimOrNull(dto.getImageAlt()))
                        .bg(trimOrNull(dto.getBg()))
                        .toPath(trimOrNull(dto.getToPath()))
                        .href(trimOrNull(dto.getHref()))
                        .badgesJson(toBadgesJson(dto.getBadges()))
                        .sortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                        .isActive(dto.getIsActive() == null ? Boolean.TRUE : dto.getIsActive())
                        .build()
        );
        return toDto(created);
    }

    @Transactional
    public BannerDto updateBanner(Long bannerId, BannerDto dto) {
        validate(dto);
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 배너입니다."));

        banner.setEyebrow(trimOrNull(dto.getEyebrow()));
        banner.setTitle(dto.getTitle().trim());
        banner.setPeriod(trimOrNull(dto.getPeriod()));
        banner.setImageSrc(dto.getImageSrc().trim());
        banner.setImageAlt(trimOrNull(dto.getImageAlt()));
        banner.setBg(trimOrNull(dto.getBg()));
        banner.setToPath(trimOrNull(dto.getToPath()));
        banner.setHref(trimOrNull(dto.getHref()));
        banner.setBadgesJson(toBadgesJson(dto.getBadges()));
        banner.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        banner.setIsActive(dto.getIsActive() == null ? Boolean.TRUE : dto.getIsActive());

        return toDto(banner);
    }

    @Transactional
    public void deleteBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 배너입니다."));
        bannerRepository.delete(banner);
    }

    @Transactional
    public String uploadBannerImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("업로드할 이미지 파일이 없습니다.");
        }
        if (!StringUtils.hasText(file.getContentType()) || !file.getContentType().startsWith("image/")) {
            throw new BusinessException("이미지 파일만 업로드할 수 있습니다.");
        }

        ensureDirExists(itemImgLocation);

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("banner");
        String ext = getExtension(originalName);
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path savePath = Paths.get(itemImgLocation).resolve(storedName);
        try {
            file.transferTo(savePath);
        } catch (IOException e) {
            throw new BusinessException("배너 이미지 업로드에 실패했습니다.");
        }
        return "/images/" + storedName;
    }

    private void validate(BannerDto dto) {
        if (dto == null) {
            throw new BusinessException("배너 데이터가 비어 있습니다.");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("배너 제목은 필수입니다.");
        }
        if (!StringUtils.hasText(dto.getImageSrc())) {
            throw new BusinessException("배너 이미지는 필수입니다.");
        }
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new BusinessException("정렬 순서는 0 이상이어야 합니다.");
        }
    }

    private BannerDto toDto(Banner banner) {
        return BannerDto.builder()
                .bannerId(banner.getBannerId())
                .eyebrow(banner.getEyebrow())
                .title(banner.getTitle())
                .period(banner.getPeriod())
                .imageSrc(banner.getImageSrc())
                .imageAlt(banner.getImageAlt())
                .bg(banner.getBg())
                .toPath(banner.getToPath())
                .href(banner.getHref())
                .badges(parseBadges(banner.getBadgesJson()))
                .sortOrder(banner.getSortOrder())
                .isActive(banner.getIsActive())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    private String toBadgesJson(List<BannerBadgeDto> badges) {
        try {
            List<BannerBadgeDto> normalized = badges == null
                    ? List.of()
                    : badges.stream()
                    .filter(item -> item != null && StringUtils.hasText(item.getLabel()))
                    .map(item -> BannerBadgeDto.builder()
                            .label(item.getLabel().trim())
                            .tone(StringUtils.hasText(item.getTone()) ? item.getTone().trim() : "light")
                            .build())
                    .toList();
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new BusinessException("배지 데이터 변환에 실패했습니다.");
        }
    }

    private List<BannerBadgeDto> parseBadges(String badgesJson) {
        if (!StringUtils.hasText(badgesJson)) {
            return new ArrayList<>();
        }
        try {
            List<BannerBadgeDto> parsed = objectMapper.readValue(badgesJson, BADGE_LIST_TYPE);
            return parsed == null ? new ArrayList<>() : parsed;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim();
    }

    private void ensureDirExists(String dir) {
        try {
            Files.createDirectories(Paths.get(dir));
        } catch (IOException e) {
            throw new BusinessException("이미지 저장 경로 생성에 실패했습니다.");
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1);
    }
}
