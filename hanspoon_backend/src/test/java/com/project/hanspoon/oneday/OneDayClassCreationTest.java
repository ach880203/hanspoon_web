package com.project.hanspoon.oneday;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import com.project.hanspoon.oneday.clazz.repository.ClassProductRepository;
import com.project.hanspoon.oneday.clazz.repository.ClassSessionRepository;
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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 원데이 클래스/세션 생성 흐름 통합 테스트입니다.
 *
 * 테스트 목적:
 * 1) 테스트 전용 사용자 + 강사 fixture 생성
 * 2) 클래스 4개 생성 (ALWAYS 3개 + EVENT 1개)
 * 3) 각 클래스에 AM/PM 세션 2개씩 생성
 * 4) 생성 건수를 "증분(delta)" 방식으로 검증
 *
 * @Transactional:
 * - 테스트 종료 시 롤백되어 실제 DB 누적 오염을 방지합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
//@Transactional
class OneDayClassCreationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ClassProductRepository classProductRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    /**
     * 테스트마다 고유한 데이터 생성을 위해 UUID 기반 시드값 사용
     */
    private String uniqueSeed;

    @BeforeEach
    void setUp() {
        // 이메일(50), 사용자명(30) 길이 제한을 넘지 않도록 짧은 시드만 사용합니다.
        uniqueSeed = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Test
    @DisplayName("원데이 클래스 생성: ALWAYS 3개 + EVENT 1개, 각 클래스 AM/PM 세션 생성")
    void createOneDayClassesWithSessionsAndEvent() {
        // given
        Instructor instructor = createTestInstructor();

        // 테스트 시작 시점 카운트(절대값이 아닌 증분 검증용)
        long beforeClassCount = classProductRepository.count();
        long beforeSessionCount = classSessionRepository.count();

        // when
        // ALWAYS 타입 3개 생성
        createClassWithTwoSessions(
                "초급 한식 클래스-" + uniqueSeed,
                "초급자를 위한 한식 기본기 수업",
                Level.BEGINNER,
                RunType.ALWAYS,
                RecipeCategory.KOREAN,
                instructor,
                LocalDateTime.now().plusDays(3)
        );

        createClassWithTwoSessions(
                "중급 베이킹 클래스-" + uniqueSeed,
                "중급자를 위한 베이킹 실습 수업",
                Level.INTERMEDIATE,
                RunType.ALWAYS,
                RecipeCategory.BAKERY,
                instructor,
                LocalDateTime.now().plusDays(4)
        );

        createClassWithTwoSessions(
                "고급 한식 클래스-" + uniqueSeed,
                "고급자를 위한 플레이팅 심화 수업",
                Level.ADVANCED,
                RunType.ALWAYS,
                RecipeCategory.KOREAN,
                instructor,
                LocalDateTime.now().plusDays(5)
        );

        // EVENT 타입 1개 생성
        createClassWithTwoSessions(
                "이벤트 베이킹 클래스-" + uniqueSeed,
                "이벤트 전용 한정 클래스",
                Level.BEGINNER,
                RunType.EVENT,
                RecipeCategory.BAKERY,
                instructor,
                LocalDateTime.now().plusDays(2)
        );

        // then
        long afterClassCount = classProductRepository.count();
        long afterSessionCount = classSessionRepository.count();

        long createdClassDelta = afterClassCount - beforeClassCount;
        long createdSessionDelta = afterSessionCount - beforeSessionCount;

        // 클래스는 정확히 4개, 세션은 클래스당 2개씩 총 8개가 생성되어야 합니다.
        Assertions.assertEquals(4, createdClassDelta, "테스트에서 생성된 클래스 수는 정확히 4개여야 합니다.");
        Assertions.assertEquals(8, createdSessionDelta, "테스트에서 생성된 세션 수는 정확히 8개여야 합니다.");
    }

    /**
     * 테스트용 강사 fixture를 생성합니다.
     *
     * 포인트:
     * - 기존 DB의 강사 데이터에 의존하지 않고, 테스트가 스스로 필요한 데이터를 만듭니다.
     * - 이렇게 해야 테스트가 환경에 덜 민감하고 재현성이 좋아집니다.
     */
    private Instructor createTestInstructor() {
        User user = userRepository.save(
                User.builder()
                        .email("oneday-instructor-" + uniqueSeed + "@example.com")
                        .password("encoded-test-password")
                        .userName("원데이강사-" + uniqueSeed)
                        .phone("010-1111-2222")
                        .address("Seoul")
                        .build()
        );

        return instructorRepository.save(
                Instructor.builder()
                        .user(user)
                        .bio("원데이 클래스 테스트 강사")
                        .build()
        );
    }

    /**
     * 클래스 1개 + 세션 2개(AM/PM)를 생성하는 헬퍼 메서드입니다.
     *
     * 가독성을 위해 테스트 본문에서 반복되는 생성 코드를 분리했습니다.
     */
    private void createClassWithTwoSessions(
            String title,
            String description,
            Level level,
            RunType runType,
            RecipeCategory category,
            Instructor instructor,
            LocalDateTime baseDateTime
    ) {
        ClassProduct classProduct = classProductRepository.save(
                ClassProduct.builder()
                        .title(title)
                        .description(description)
                        .level(level)
                        .runType(runType)
                        .category(category)
                        .instructor(instructor)
                        .build()
        );

        // 오전 세션(AM)
        classSessionRepository.save(
                ClassSession.builder()
                        .classProduct(classProduct)
                        .startAt(baseDateTime.withHour(10).withMinute(0).withSecond(0).withNano(0))
                        .slot(SessionSlot.AM)
                        .capacity(10)
                        .price(50000)
                        .build()
        );

        // 오후 세션(PM)
        classSessionRepository.save(
                ClassSession.builder()
                        .classProduct(classProduct)
                        .startAt(baseDateTime.withHour(15).withMinute(0).withSecond(0).withNano(0))
                        .slot(SessionSlot.PM)
                        .capacity(10)
                        .price(55000)
                        .build()
        );
    }
}
