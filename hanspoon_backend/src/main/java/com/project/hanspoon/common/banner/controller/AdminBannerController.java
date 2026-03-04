package com.project.hanspoon.common.banner.controller;

import com.project.hanspoon.common.banner.dto.BannerDto;
import com.project.hanspoon.common.banner.service.BannerService;
import com.project.hanspoon.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<BannerDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.getAllBannersForAdmin()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerDto>> get(@PathVariable("id") Long bannerId) {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.getBanner(bannerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BannerDto>> create(@RequestBody BannerDto dto) {
        return ResponseEntity.ok(ApiResponse.ok("배너가 등록되었습니다.", bannerService.createBanner(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerDto>> update(@PathVariable("id") Long bannerId, @RequestBody BannerDto dto) {
        return ResponseEntity.ok(ApiResponse.ok("배너가 수정되었습니다.", bannerService.updateBanner(bannerId, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.ok("배너가 삭제되었습니다."));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("이미지가 업로드되었습니다.", bannerService.uploadBannerImage(file)));
    }
}
