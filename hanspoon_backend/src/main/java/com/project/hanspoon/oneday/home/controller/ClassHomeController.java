package com.project.hanspoon.oneday.home.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.oneday.home.dto.ClassHomeResponse;
import com.project.hanspoon.oneday.home.service.ClassHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oneday/home")
public class ClassHomeController {

    private final ClassHomeService homeService;

    @GetMapping
    public ApiResponse<ClassHomeResponse> home() {
        return ApiResponse.ok(homeService.home());
    }
}
