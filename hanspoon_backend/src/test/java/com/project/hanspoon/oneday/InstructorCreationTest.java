package com.project.hanspoon.oneday;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.oneday.instructor.entity.Instructor;
import com.project.hanspoon.oneday.instructor.repository.InstructorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 강사(Instructor) 생성/조회 흐름 테스트입니다.
 *
 * 테스트 목적:
 * 1) 사용자(User) 생성
 * 2) 사용자 기반 강사(Instructor) 생성
 * 3) userId로 강사 조회 시, 저장한 데이터와 일치하는지 검증
 *
 * @Transactional:
 * - 테스트 종료 시 자동 롤백되어 DB 오염을 방지합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
//@Transactional
class InstructorCreationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    /**
     * 이메일 unique 제약 충돌을 피하기 위한 테스트용 시드값입니다.
     * 테스트마다 UUID를 새로 생성합니다.
     */
    private String uniqueSeed;

    @BeforeEach
    void setUp() {
        // 이메일(50), 사용자명(30) 길이 제한을 넘지 않도록 짧은 시드만 사용합니다.
        uniqueSeed = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Test
    @DisplayName("강사 생성: 사용자 생성 후 강사 등록 및 userId 조회")
    void createInstructor() {
        // given
        User user = createTestUser();
        String expectedBio = "원데이 테스트 강사 소개";

        // when
        Instructor savedInstructor = instructorRepository.save(
                Instructor.builder()
                        .user(user)
                        .bio(expectedBio)
                        .build()
        );

        // then
        Assertions.assertNotNull(savedInstructor.getId(), "강사 저장 후 ID가 생성되어야 합니다.");

        Instructor foundInstructor = instructorRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new AssertionError("userId로 강사를 조회해야 합니다."));

        Assertions.assertEquals(savedInstructor.getId(), foundInstructor.getId(), "저장한 강사와 조회한 강사 ID가 같아야 합니다.");
        Assertions.assertEquals(expectedBio, foundInstructor.getBio(), "강사 소개(bio)는 저장값과 같아야 합니다.");
        Assertions.assertEquals(user.getUserId(), foundInstructor.getUser().getUserId(), "강사-사용자 연결 키(userId)가 유지되어야 합니다.");
    }

    /**
     * 테스트용 사용자(User) 생성 헬퍼 메서드입니다.
     *
     * 분리 이유:
     * - 테스트 본문에서 핵심 의도(강사 생성 검증)에 집중하기 위함
     * - 반복 코드를 줄여 가독성과 유지보수성을 높이기 위함
     */
    private User createTestUser() {
        return userRepository.save(
                User.builder()
                        .email("instructor-test-" + uniqueSeed + "@example.com")
                        .password("encoded-test-password")
                        .userName("강사테스트-" + uniqueSeed)
                        .phone("010-0000-0000")
                        .address("Seoul")
                        .build()
        );
    }
}
