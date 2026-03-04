package com.project.hanspoon.recipe.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.recipe.dto.RecipeReviewAnswerRequest;
import com.project.hanspoon.recipe.dto.RecipeReviewCreateRequest;
import com.project.hanspoon.recipe.dto.RecipeReviewResponse;
import com.project.hanspoon.recipe.dto.RecipeReviewUpdateRequest;
import com.project.hanspoon.recipe.service.RecipeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe/reviews")
public class RecipeReviewController {

    private final RecipeReviewService recipeReviewService;

    @PostMapping
    public ApiResponse<RecipeReviewResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RecipeReviewCreateRequest req
    ) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok("레시피 리뷰가 등록되었습니다.", recipeReviewService.create(userId, req));
    }

    @PatchMapping("/{reviewId}")
    public ApiResponse<RecipeReviewResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @RequestBody RecipeReviewUpdateRequest req
    ) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok("레시피 리뷰가 수정되었습니다.", recipeReviewService.update(userId, reviewId, req));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        Long userId = requireUserId(userDetails);
        recipeReviewService.delete(userId, reviewId);
        return ApiResponse.ok("레시피 리뷰가 삭제되었습니다.", null);
    }

    @PostMapping("/{reviewId}/answer")
    public ApiResponse<RecipeReviewResponse> answer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @RequestBody RecipeReviewAnswerRequest req
    ) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(
                "레시피 리뷰 답글이 등록되었습니다.",
                recipeReviewService.answer(userId, isAdmin, reviewId, req)
        );
    }

    @GetMapping("/recipes/{recipeId}")
    public ApiResponse<List<RecipeReviewResponse>> listByRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerUserId = userDetails != null ? userDetails.getUserId() : null;
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(recipeReviewService.listByRecipe(recipeId, viewerUserId, isAdmin));
    }

    @GetMapping("/admin")
    public ApiResponse<List<RecipeReviewResponse>> listForAdmin(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(recipeReviewService.listAllForAdmin(userId, isAdmin));
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
