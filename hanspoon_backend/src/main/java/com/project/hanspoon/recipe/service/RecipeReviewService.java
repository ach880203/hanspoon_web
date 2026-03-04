package com.project.hanspoon.recipe.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.recipe.dto.RecipeReviewAnswerRequest;
import com.project.hanspoon.recipe.dto.RecipeReviewCreateRequest;
import com.project.hanspoon.recipe.dto.RecipeReviewResponse;
import com.project.hanspoon.recipe.dto.RecipeReviewUpdateRequest;
import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeRev;
import com.project.hanspoon.recipe.repository.RecipeRepository;
import com.project.hanspoon.recipe.repository.RecipeRevRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeReviewService {
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final RecipeRepository recipeRepository;
    private final RecipeRevRepository recipeRevRepository;
    private final UserRepository userRepository;

    public RecipeReviewResponse create(Long userId, RecipeReviewCreateRequest req) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        validateCreate(req);

        Recipe recipe = recipeRepository.findById(req.recipeId())
                .orElseThrow(() -> new BusinessException("레시피를 찾을 수 없습니다. id=" + req.recipeId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자 정보를 찾을 수 없습니다."));

        RecipeRev review = new RecipeRev();
        review.setRecipe(recipe);
        review.setUser(user);
        review.setRating(req.rating());
        review.setContent(req.content().trim());

        RecipeRev saved = recipeRevRepository.save(review);
        return toResponse(saved, user.getUserName(), null, false);
    }

    public RecipeReviewResponse update(Long userId, Long reviewId, RecipeReviewUpdateRequest req) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (reviewId == null || reviewId <= 0) {
            throw new BusinessException("리뷰 ID가 올바르지 않습니다.");
        }
        if (req == null) {
            throw new BusinessException("리뷰 수정 요청값이 없습니다.");
        }

        RecipeRev review = recipeRevRepository.findByIdAndDelFlagFalse(reviewId)
                .orElseThrow(() -> new BusinessException("리뷰를 찾을 수 없습니다."));
        if (review.getUser() == null || !review.getUser().getUserId().equals(userId)) {
            throw new BusinessException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        int nextRating = req.rating() == null ? review.getRating() : req.rating();
        if (nextRating < 1 || nextRating > 5) {
            throw new BusinessException("평점은 1~5 사이여야 합니다.");
        }

        String nextContent = req.content() == null ? review.getContent() : req.content().trim();
        if (nextContent == null || nextContent.isBlank()) {
            throw new BusinessException("리뷰 내용은 필수입니다.");
        }

        review.updateReview(nextRating, nextContent);
        return toResponse(review, resolveUserName(userId), resolveUserName(review.getAnsweredByUserId()), false);
    }

    public void delete(Long userId, Long reviewId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (reviewId == null || reviewId <= 0) {
            throw new BusinessException("리뷰 ID가 올바르지 않습니다.");
        }

        RecipeRev review = recipeRevRepository.findByIdAndDelFlagFalse(reviewId)
                .orElseThrow(() -> new BusinessException("리뷰를 찾을 수 없습니다."));
        if (review.getUser() == null || !review.getUser().getUserId().equals(userId)) {
            throw new BusinessException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.markDeleted(LocalDateTime.now(KST_ZONE));
    }

    public RecipeReviewResponse answer(
            Long actorUserId,
            boolean isAdmin,
            Long reviewId,
            RecipeReviewAnswerRequest req
    ) {
        if (actorUserId == null || actorUserId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (!isAdmin) {
            throw new BusinessException("관리자만 리뷰 답글을 등록할 수 있습니다.");
        }
        if (reviewId == null || reviewId <= 0) {
            throw new BusinessException("리뷰 ID가 올바르지 않습니다.");
        }
        if (req == null || req.answerContent() == null || req.answerContent().isBlank()) {
            throw new BusinessException("답글 내용을 입력해 주세요.");
        }

        RecipeRev review = recipeRevRepository.findByIdAndDelFlagFalse(reviewId)
                .orElseThrow(() -> new BusinessException("리뷰를 찾을 수 없습니다."));

        String answer = req.answerContent().trim();
        if (answer.length() > 2000) {
            throw new BusinessException("답글 내용은 최대 2000자입니다.");
        }

        review.answer(answer, actorUserId, LocalDateTime.now(KST_ZONE));
        return toResponse(
                review,
                resolveUserName(review.getUser() != null ? review.getUser().getUserId() : null),
                resolveUserName(actorUserId),
                true
        );
    }

    @Transactional(readOnly = true)
    public List<RecipeReviewResponse> listByRecipe(Long recipeId, Long viewerUserId, boolean isAdmin) {
        if (recipeId == null || recipeId <= 0) {
            throw new BusinessException("레시피 ID가 올바르지 않습니다.");
        }

        List<RecipeRev> reviews = recipeRevRepository.findAllByRecipe_IdAndDelFlagFalseOrderByIdDesc(recipeId);
        Map<Long, String> names = buildNameMap(reviews);
        boolean canAnswer = isAdmin && viewerUserId != null && viewerUserId > 0;

        return reviews.stream()
                .map(review -> toResponse(
                        review,
                        names.getOrDefault(getUserId(review), "이름 없음"),
                        names.getOrDefault(review.getAnsweredByUserId(), "관리자"),
                        canAnswer
                ))
                .toList();
    }

//    @Transactional(readOnly = true)
//    public List<RecipeReviewResponse> listMy(Long userId, boolean isAdmin) {
//        if (userId == null || userId <= 0) {
//            throw new BusinessException("로그인 정보가 필요합니다.");
//        }
//
//        List<RecipeRev> reviews = recipeRevRepository.findAllByUser_UserIdAndDelFlagFalseOrderByIdDesc(userId);
//        Map<Long, String> names = buildNameMap(reviews);
//
//        return reviews.stream()
//                .map(review -> toResponse(
//                        review,
//                        names.getOrDefault(getUserId(review), "이름 없음"),
//                        names.getOrDefault(review.getAnsweredByUserId(), "관리자"),
//                        isAdmin
//                ))
//                .toList();
//    }

    @Transactional(readOnly = true)
    public List<RecipeReviewResponse> listAllForAdmin(Long adminUserId, boolean isAdmin) {
        if (adminUserId == null || adminUserId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (!isAdmin) {
            throw new BusinessException("관리자 권한이 필요합니다.");
        }

        List<RecipeRev> reviews = recipeRevRepository.findAllByDelFlagFalseOrderByIdDesc();
        Map<Long, String> names = buildNameMap(reviews);

        return reviews.stream()
                .map(review -> toResponse(
                        review,
                        names.getOrDefault(getUserId(review), "이름 없음"),
                        names.getOrDefault(review.getAnsweredByUserId(), "관리자"),
                        true
                ))
                .toList();
    }

    private void validateCreate(RecipeReviewCreateRequest req) {
        if (req == null) {
            throw new BusinessException("리뷰 요청값이 없습니다.");
        }
        if (req.recipeId() == null || req.recipeId() <= 0) {
            throw new BusinessException("레시피 ID는 필수입니다.");
        }
        if (req.rating() < 1 || req.rating() > 5) {
            throw new BusinessException("평점은 1~5 사이여야 합니다.");
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new BusinessException("리뷰 내용은 필수입니다.");
        }
    }

    private Map<Long, String> buildNameMap(List<RecipeRev> reviews) {
        List<Long> userIds = reviews.stream()
                .flatMap(review -> java.util.stream.Stream.of(getUserId(review), review.getAnsweredByUserId()))
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUserName, (a, b) -> a));
    }

    private Long getUserId(RecipeRev review) {
        return review.getUser() != null ? review.getUser().getUserId() : null;
    }

    private String resolveUserName(Long userId) {
        if (userId == null || userId <= 0) {
            return "이름 없음";
        }
        return userRepository.findById(userId)
                .map(User::getUserName)
                .orElse("이름 없음");
    }

    private RecipeReviewResponse toResponse(
            RecipeRev review,
            String reviewerName,
            String answeredByName,
            boolean canAnswer
    ) {
        Long recipeId = review.getRecipe() != null ? review.getRecipe().getId() : null;
        Long userId = getUserId(review);
        String recipeTitle = (review.getRecipe() != null) ? review.getRecipe().getTitle() : "정보 없음";
        return new RecipeReviewResponse(
                review.getId(),
                recipeId,
                userId,
                reviewerName,
                recipeTitle,
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getAnswerContent(),
                review.getAnsweredByUserId(),
                review.getAnsweredByUserId() == null ? null : answeredByName,
                review.getAnsweredAt(),
                canAnswer
        );
    }
}
