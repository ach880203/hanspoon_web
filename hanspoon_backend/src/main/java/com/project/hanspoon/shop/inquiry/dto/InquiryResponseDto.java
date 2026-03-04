package com.project.hanspoon.shop.inquiry.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponseDto {

    private Long inqId;
    private Long productId;
    private String productName;
    private Long userId;

    private String content;
    private String answer;
    private Boolean answeredYn;

    private Boolean secret;
    private Boolean canViewSecret;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
}
