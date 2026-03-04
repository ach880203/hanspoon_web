package com.project.hanspoon.recipe.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.recipe.dto.RecipeInquiryAnswerRequest;
import com.project.hanspoon.recipe.dto.RecipeInquiryCreateRequest;
import com.project.hanspoon.recipe.dto.RecipeInquiryResponse;
import com.project.hanspoon.recipe.dto.RecipeInquiryUpdateRequest;
import com.project.hanspoon.recipe.service.RecipeInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe/inquiries")
public class RecipeInquiryController {

    private final RecipeInquiryService recipeInquiryService;

    @PostMapping
    public ApiResponse<RecipeInquiryResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RecipeInquiryCreateRequest req
    ) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok("레시피 문의가 등록되었습니다.", recipeInquiryService.create(userId, req));
    }

    @PatchMapping("/{inquiryId}")
    public ApiResponse<RecipeInquiryResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inquiryId,
            @RequestBody RecipeInquiryUpdateRequest req
    ) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok("레시피 문의가 수정되었습니다.", recipeInquiryService.update(userId, inquiryId, req));
    }

    @DeleteMapping("/{inquiryId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inquiryId
    ) {
        Long userId = requireUserId(userDetails);
        recipeInquiryService.delete(userId, inquiryId);
        return ApiResponse.ok("레시피 문의가 삭제되었습니다.", null);
    }

    @PostMapping("/{inquiryId}/answer")
    public ApiResponse<RecipeInquiryResponse> answer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inquiryId,
            @RequestBody RecipeInquiryAnswerRequest req
    ) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(
                "레시피 문의 답글이 등록되었습니다.",
                recipeInquiryService.answer(userId, isAdmin, inquiryId, req)
        );
    }

    @GetMapping("/recipes/{recipeId}")
    public ApiResponse<List<RecipeInquiryResponse>> listByRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerUserId = userDetails != null ? userDetails.getUserId() : null;
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(recipeInquiryService.listByRecipe(recipeId, viewerUserId, isAdmin));
    }

    @GetMapping("/me")
    public ApiResponse<List<RecipeInquiryResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(recipeInquiryService.listMy(userId, isAdmin));
    }

    @GetMapping("/admin")
    public ApiResponse<List<RecipeInquiryResponse>> listForAdmin(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(recipeInquiryService.listAllForAdmin(userId, isAdmin));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }

    private boolean hasRole(CustomUserDetails userDetails, String... candidates) {
        if (userDetails == null || userDetails.getAuthorities() == null) return false;
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            String role = authority.getAuthority();
            if (role == null) continue;
            for (String candidate : candidates) {
                if (candidate.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
