package com.project.hanspoon.shop.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryAnswerRequestDto {

    @NotBlank
    private String answer;
}
