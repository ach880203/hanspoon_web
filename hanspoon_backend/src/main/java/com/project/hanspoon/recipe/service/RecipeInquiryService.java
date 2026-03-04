package com.project.hanspoon.recipe.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.recipe.dto.RecipeInquiryAnswerRequest;
import com.project.hanspoon.recipe.dto.RecipeInquiryCreateRequest;
import com.project.hanspoon.recipe.dto.RecipeInquiryResponse;
import com.project.hanspoon.recipe.dto.RecipeInquiryUpdateRequest;
import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeIng;
import com.project.hanspoon.recipe.repository.RecipeIngRepository;
import com.project.hanspoon.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeInquiryService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngRepository recipeIngRepository;
    private final UserRepository userRepository;

    public RecipeInquiryResponse create(Long userId, RecipeInquiryCreateRequest req) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        validateCreate(req);

        Recipe recipe = recipeRepository.findById(req.recipeId())
                .orElseThrow(() -> new BusinessException("레시피를 찾을 수 없습니다. id=" + req.recipeId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자 정보를 찾을 수 없습니다."));

        RecipeIng inquiry = new RecipeIng();
        inquiry.setRecipe(recipe);
        inquiry.setUser(user);
        inquiry.setCategory(normalizeCategory(req.category()));
        inquiry.setTitle(req.title().trim());
        inquiry.setContent(req.content().trim());
        inquiry.setSecret(req.secret());
        inquiry.setAnswered(false);

        RecipeIng saved = recipeIngRepository.save(inquiry);
        return toResponse(saved, user.getUserName(), true, false);
    }

    public RecipeInquiryResponse update(Long userId, Long inquiryId, RecipeInquiryUpdateRequest req) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (inquiryId == null || inquiryId <= 0) {
            throw new BusinessException("문의 ID가 올바르지 않습니다.");
        }
        if (req == null) {
            throw new BusinessException("문의 수정 요청값이 없습니다.");
        }

        RecipeIng inquiry = recipeIngRepository.findByIdAndUser_UserId(inquiryId, userId)
                .orElseThrow(() -> new BusinessException("문의를 찾을 수 없습니다."));

        String nextCategory = req.category() == null ? inquiry.getCategory() : normalizeCategory(req.category());
        String nextTitle = req.title() == null ? inquiry.getTitle() : req.title().trim();
        String nextContent = req.content() == null ? inquiry.getContent() : req.content().trim();
        boolean nextSecret = req.secret() == null ? inquiry.isSecret() : req.secret();

        if (nextTitle == null || nextTitle.isBlank()) {
            throw new BusinessException("문의 제목은 필수입니다.");
        }
        if (nextContent == null || nextContent.isBlank()) {
            throw new BusinessException("문의 내용은 필수입니다.");
        }
        if (nextTitle.length() > 150) {
            throw new BusinessException("문의 제목은 최대 150자입니다.");
        }
        if (nextContent.length() > 4000) {
            throw new BusinessException("문의 내용은 최대 4000자입니다.");
        }

        inquiry.updateQuestion(nextCategory, nextTitle, nextContent, nextSecret);
        return toResponse(
                inquiry,
                resolveUserName(userId),
                true,
                false
        );
    }

    public void delete(Long userId, Long inquiryId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (inquiryId == null || inquiryId <= 0) {
            throw new BusinessException("문의 ID가 올바르지 않습니다.");
        }

        RecipeIng inquiry = recipeIngRepository.findByIdAndUser_UserId(inquiryId, userId)
                .orElseThrow(() -> new BusinessException("문의를 찾을 수 없습니다."));
        recipeIngRepository.delete(inquiry);
    }

    public RecipeInquiryResponse answer(
            Long actorUserId,
            boolean isAdmin,
            Long inquiryId,
            RecipeInquiryAnswerRequest req
    ) {
        if (actorUserId == null || actorUserId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (!isAdmin) {
            throw new BusinessException("관리자만 문의 답글을 등록할 수 있습니다.");
        }
        if (inquiryId == null || inquiryId <= 0) {
            throw new BusinessException("문의 ID가 올바르지 않습니다.");
        }
        if (req == null || req.answerContent() == null || req.answerContent().isBlank()) {
            throw new BusinessException("답글 내용을 입력해 주세요.");
        }

        RecipeIng inquiry = recipeIngRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException("문의를 찾을 수 없습니다. id=" + inquiryId));

        String answer = req.answerContent().trim();
        if (answer.length() > 4000) {
            throw new BusinessException("답글 내용은 최대 4000자입니다.");
        }

        inquiry.answerQuestion(answer, actorUserId, LocalDateTime.now());
        return toResponse(
                inquiry,
                resolveUserName(inquiry.getUser() != null ? inquiry.getUser().getUserId() : null),
                true,
                true
        );
    }

    @Transactional(readOnly = true)
    public List<RecipeInquiryResponse> listByRecipe(Long recipeId, Long viewerUserId, boolean isAdmin) {
        if (recipeId == null || recipeId <= 0) {
            throw new BusinessException("레시피 ID가 올바르지 않습니다.");
        }
        List<RecipeIng> inquiries = recipeIngRepository.findAllByRecipe_IdOrderByIdDesc(recipeId);
        Map<Long, String> names = buildNameMap(inquiries);
        boolean canAnswer = isAdmin && viewerUserId != null && viewerUserId > 0;

        return inquiries.stream()
                .map(inquiry -> {
                    boolean canView = canViewInquiry(inquiry, viewerUserId, isAdmin);
                    return toResponse(
                            inquiry,
                            names.getOrDefault(getUserId(inquiry), "이름 없음"),
                            canView,
                            canAnswer
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeInquiryResponse> listMy(Long userId, boolean isAdmin) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }

        List<RecipeIng> inquiries = recipeIngRepository.findAllByUser_UserIdOrderByIdDesc(userId);
        Map<Long, String> names = buildNameMap(inquiries);

        return inquiries.stream()
                .map(inquiry -> toResponse(
                        inquiry,
                        names.getOrDefault(getUserId(inquiry), "이름 없음"),
                        true,
                        isAdmin
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeInquiryResponse> listAllForAdmin(Long adminUserId, boolean isAdmin) {
        if (adminUserId == null || adminUserId <= 0) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        if (!isAdmin) {
            throw new BusinessException("관리자 권한이 필요합니다.");
        }

        List<RecipeIng> inquiries = recipeIngRepository.findAllByOrderByIdDesc();
        Map<Long, String> names = buildNameMap(inquiries);

        return inquiries.stream()
                .map(inquiry -> toResponse(
                        inquiry,
                        names.getOrDefault(getUserId(inquiry), "이름 없음"),
                        true,
                        true
                ))
                .toList();
    }

    private void validateCreate(RecipeInquiryCreateRequest req) {
        if (req == null) {
            throw new BusinessException("문의 요청값이 없습니다.");
        }
        if (req.recipeId() == null || req.recipeId() <= 0) {
            throw new BusinessException("레시피 ID는 필수입니다.");
        }
        if (req.title() == null || req.title().isBlank()) {
            throw new BusinessException("문의 제목은 필수입니다.");
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new BusinessException("문의 내용은 필수입니다.");
        }
        if (req.title().trim().length() > 150) {
            throw new BusinessException("문의 제목은 최대 150자입니다.");
        }
        if (req.content().trim().length() > 4000) {
            throw new BusinessException("문의 내용은 최대 4000자입니다.");
        }
    }

    private boolean canViewInquiry(RecipeIng inquiry, Long viewerUserId, boolean isAdmin) {
        if (!inquiry.isSecret()) {
            return true;
        }
        if (viewerUserId == null || viewerUserId <= 0) {
            return false;
        }
        Long writerId = getUserId(inquiry);
        return isAdmin || (writerId != null && writerId.equals(viewerUserId));
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "일반";
        }
        String normalized = category.trim();
        if (normalized.length() > 30) {
            throw new BusinessException("문의 분류는 최대 30자입니다.");
        }
        return normalized;
    }

    private Map<Long, String> buildNameMap(List<RecipeIng> inquiries) {
        List<Long> userIds = inquiries.stream()
                .flatMap(inquiry -> java.util.stream.Stream.of(getUserId(inquiry), inquiry.getAnsweredByUserId()))
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUserName, (a, b) -> a));
    }

    private Long getUserId(RecipeIng inquiry) {
        return inquiry.getUser() != null ? inquiry.getUser().getUserId() : null;
    }

    private String resolveUserName(Long userId) {
        if (userId == null || userId <= 0) {
            return "이름 없음";
        }
        return userRepository.findById(userId)
                .map(User::getUserName)
                .orElse("이름 없음");
    }

    private RecipeInquiryResponse toResponse(
            RecipeIng inquiry,
            String writerName,
            boolean canViewContent,
            boolean canAnswer
    ) {
        Long recipeId = inquiry.getRecipe() != null ? inquiry.getRecipe().getId() : null;
        Long userId = getUserId(inquiry);

        String title = canViewContent ? safeText(inquiry.getTitle(), "제목 없음") : "비밀글입니다.";
        String content = canViewContent ? safeText(inquiry.getContent(), "문의 내용이 없습니다.") : "비밀글입니다.";
        String answerContent = canViewContent ? inquiry.getAnswer() : null;
        String answeredByName = inquiry.getAnsweredByUserId() == null
                ? null
                : resolveUserName(inquiry.getAnsweredByUserId());

        return new RecipeInquiryResponse(
                inquiry.getId(),
                recipeId,
                userId,
                writerName,
                inquiry.getRecipe().getTitle(),
                safeText(inquiry.getCategory(), "일반"),
                title,
                content,
                inquiry.isSecret(),
                inquiry.isAnswered(),
                answerContent,
                inquiry.getAnsweredByUserId(),
                answeredByName,
                inquiry.getAnsweredAt(),
                canAnswer,
                inquiry.getCreatedAt()
        );
    }

    private String safeText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
