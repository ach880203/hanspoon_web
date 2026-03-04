package com.project.hanspoon;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class HanspoonApplication {

    public static void main(String[] args) {
        // .env 파일 로드하여 시스템 프로퍼티로 설정
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(HanspoonApplication.class, args);
    }

}
