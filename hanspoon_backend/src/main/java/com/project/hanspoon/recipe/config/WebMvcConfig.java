package com.project.hanspoon.recipe.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Log4j2
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${uploadPath}")
    private String uploadPath;

    @Value("${FRONTEND_URL:https://hanspoon.store}")
    private String frontendUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // application.yml 의 uploadPath(file:///c:/hanspoon/img)를 그대로 사용한다.
        // trailing slash를 강제로 붙이지 않아 ResourceHandler 경고를 방지한다.
        log.info("매핑된 로컬 경로: {}", uploadPath);

        registry.addResourceHandler("/images/recipe/**")
                .addResourceLocations(uploadPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("http://127.0.0.1:5173");

        // 운영 도메인과 로컬 개발 도메인을 함께 열어 두어 OAuth 복귀 후 API 호출이 막히지 않게 합니다.
        String normalizedFrontendUrl = frontendUrl == null ? "" : frontendUrl.trim().replaceAll("/+$", "");
        if (!normalizedFrontendUrl.isBlank()) {
            allowedOrigins.add(normalizedFrontendUrl);
            if ("https://hanspoon.store".equalsIgnoreCase(normalizedFrontendUrl)) {
                allowedOrigins.add("https://www.hanspoon.store");
            }
        }

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
