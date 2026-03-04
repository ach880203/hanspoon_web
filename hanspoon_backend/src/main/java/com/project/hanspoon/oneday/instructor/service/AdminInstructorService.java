package com.project.hanspoon.oneday.instructor.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.oneday.clazz.repository.ClassProductRepository;
import com.project.hanspoon.oneday.instructor.dto.InstructorAdminRequest;
import com.project.hanspoon.oneday.instructor.dto.InstructorAdminResponse;
import com.project.hanspoon.oneday.instructor.dto.InstructorCandidateUserResponse;
import com.project.hanspoon.oneday.instructor.entity.Instructor;
import com.project.hanspoon.oneday.instructor.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInstructorService {
    private static final int MAX_PROFILE_IMAGE_DATA_LENGTH = 72_000_000; // Base64(DataURL) 기준, 약 50MB 원본 이미지 허용

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final ClassProductRepository classProductRepository;

    @Transactional(readOnly = true)
    public List<InstructorAdminResponse> getInstructors() {
        return instructorRepository.findAll().stream()
                .sorted(Comparator.comparing(Instructor::getId).reversed())
                .map(InstructorAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstructorCandidateUserResponse> getCandidateUsers(String keyword) {
        String k = normalizeKeyword(keyword);
        return userRepository.findAll().stream()
                .filter(user -> Boolean.FALSE.equals(user.getIsDeleted()) || user.getIsDeleted() == null)
                .filter(user -> user.getEmail() != null)
                .filter(user -> user.getRole() == null || !"ROLE_ADMIN".equalsIgnoreCase(user.getRole()))
                .filter(user -> k.isEmpty()
                        || containsIgnoreCase(user.getUserName(), k)
                        || containsIgnoreCase(user.getEmail(), k))
                .sorted(Comparator.comparing(User::getUserId).reversed())
                .limit(200)
                .map(InstructorCandidateUserResponse::from)
                .toList();
    }

    public InstructorAdminResponse createInstructor(InstructorAdminRequest req) {
        if (req == null) throw new BusinessException("요청 데이터가 비어 있습니다.");
        if (req.userId() == null || req.userId() <= 0) throw new BusinessException("회원 ID를 선택해 주세요.");
        if (instructorRepository.existsByUser_UserId(req.userId())) {
            throw new BusinessException("이미 강사로 등록된 회원입니다.");
        }

        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new BusinessException("회원을 찾을 수 없습니다. userId=" + req.userId()));

        Instructor saved = instructorRepository.save(
                Instructor.builder()
                        .user(user)
                        .bio(normalizeBio(req.bio()))
                        .specialty(trimOrEmpty(req.specialty()))
                        .career(trimOrEmpty(req.career()))
                        .profileImageData(normalizeProfileImageData(req.profileImageData()))
                        .build()
        );

        ensureInstructorRole(user);
        return InstructorAdminResponse.from(saved);
    }

    public InstructorAdminResponse updateInstructor(Long instructorId, InstructorAdminRequest req) {
        if (instructorId == null || instructorId <= 0) throw new BusinessException("강사 ID가 올바르지 않습니다.");
        if (req == null) throw new BusinessException("요청 데이터가 비어 있습니다.");

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new BusinessException("강사를 찾을 수 없습니다. instructorId=" + instructorId));

        User user = instructor.getUser();
        if (req.userId() != null && req.userId() > 0 && !req.userId().equals(user.getUserId())) {
            throw new BusinessException("강사에 연결된 회원은 변경할 수 없습니다. 삭제 후 다시 등록해 주세요.");
        }

        instructor.updateProfile(
                normalizeBio(req.bio()),
                trimOrEmpty(req.specialty()),
                trimOrEmpty(req.career()),
                normalizeProfileImageData(req.profileImageData())
        );
        ensureInstructorRole(user);
        return InstructorAdminResponse.from(instructor);
    }

    public void deleteInstructor(Long instructorId) {
        if (instructorId == null || instructorId <= 0) throw new BusinessException("강사 ID가 올바르지 않습니다.");

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new BusinessException("강사를 찾을 수 없습니다. instructorId=" + instructorId));

        if (classProductRepository.existsByInstructor_Id(instructorId)) {
            throw new BusinessException("해당 강사로 등록된 클래스가 있어 삭제할 수 없습니다.");
        }

        User user = instructor.getUser();
        instructorRepository.delete(instructor);

        if (user != null && "ROLE_INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
            user.setRole("ROLE_USER");
        }
    }

    private void ensureInstructorRole(User user) {
        if (user == null) return;
        if (!"ROLE_INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
            user.setRole("ROLE_INSTRUCTOR");
        }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String normalizeBio(String bio) {
        String normalized = trimOrEmpty(bio);
        if (normalized.isBlank()) {
            throw new BusinessException("강사 소개를 입력해 주세요.");
        }
        if (normalized.length() > 1000) {
            throw new BusinessException("강사 소개는 1000자 이내로 입력해 주세요.");
        }
        return normalized;
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeProfileImageData(String profileImageData) {
        String normalized = trimOrEmpty(profileImageData);
        if (normalized.length() > MAX_PROFILE_IMAGE_DATA_LENGTH) {
            throw new BusinessException("강사 이미지는 50MB 이하만 업로드할 수 있습니다.");
        }
        return normalized;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || keyword == null) return false;
        return source.toLowerCase().contains(keyword.toLowerCase());
    }
}
