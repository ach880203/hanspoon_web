package com.project.hanspoon.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SalesStatisticsDto {
    private List<CategoryRatio> categoryRatios;
    private List<TopItem> topItems;

    @Data
    @Builder
    public static class CategoryRatio {
        private String category; // "상품" or "클래스"
        private long value;
    }

    @Data
    @Builder
    public static class TopItem {
        private String name;
        private long sales;
        private long count;
    }
}
