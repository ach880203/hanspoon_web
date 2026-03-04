package com.project.hanspoon.shop.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequestDto {
    @NotBlank
    private String content;

    @Min(1) @Max(5)
    private Integer rating;
}
