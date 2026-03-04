package com.project.hanspoon.oneday.completion.scheduler;

import com.project.hanspoon.oneday.completion.service.ClassCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ClassCompletionScheduler {

    private final ClassCompletionService completionService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void complete(){
        completionService.completeAndIssueCoupons(LocalDateTime.now());
    }
}
