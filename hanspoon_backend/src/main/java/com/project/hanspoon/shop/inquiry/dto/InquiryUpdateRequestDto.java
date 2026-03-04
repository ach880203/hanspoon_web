package com.project.hanspoon.shop.inquiry.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryUpdateRequestDto {

    private String content;

    private Boolean secret;
}
