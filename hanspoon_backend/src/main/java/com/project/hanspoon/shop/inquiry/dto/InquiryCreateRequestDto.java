package com.project.hanspoon.shop.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryCreateRequestDto {

    @NotBlank
    private String content;

    private Boolean secret;
}
