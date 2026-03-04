package com.project.hanspoon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 부트스트랩(스프링 컨텍스트 로딩) 기본 테스트입니다.
 *
 * 이 테스트는 "서비스 전체가 최소한 기동 가능한 상태인지"를 확인합니다.
 * 초보자 관점에서 가장 먼저 필요한 안전망이기 때문에, 복잡한 로직 대신
 * 컨텍스트 로딩 성공 자체를 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HanspoonApplicationTests {

    @Test
    void contextLoads() {
        // 테스트 메서드 본문이 비어 있어도 의미가 있습니다.
        // @SpringBootTest가 성공적으로 컨텍스트를 올리지 못하면
        // 이 메서드에 도달하기 전에 테스트가 실패합니다.
    }

}
