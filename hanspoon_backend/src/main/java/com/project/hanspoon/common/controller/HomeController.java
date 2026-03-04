package com.project.hanspoon.common.controller;

import com.project.hanspoon.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 공통 REST API Controller
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    /**
     * 서버 상태 확인
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "hanspoon-api");
        
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * API 정보 조회
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, String>>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Hanspoon API");
        info.put("version", "1.0.0");
        info.put("description", "한스푼 서비스 API");
        
        return ResponseEntity.ok(ApiResponse.ok(info));
    }
}
