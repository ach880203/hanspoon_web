package com.project.hanspoon.common.banner.controller;

import com.project.hanspoon.common.banner.dto.BannerDto;
import com.project.hanspoon.common.banner.service.BannerService;
import com.project.hanspoon.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerDto>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.getActiveBanners()));
    }
}
