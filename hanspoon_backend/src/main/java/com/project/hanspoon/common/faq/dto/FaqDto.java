package com.project.hanspoon.common.faq.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqDto {
    private Long faqId;
    
    @Size(max = 50)
    private String category;
    
    private String question;
    private String answer;
}
