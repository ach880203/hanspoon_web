package com.project.hanspoon.shop.review.dto;

import lombok.*;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDto {
    private Double avgRating;              // 평균(예: 4.3)
    private Long totalCount;               // 총 리뷰 수
    private Map<Integer, Long> countsByRating; // {1:2,2:5,3:10,4:20,5:46}
}