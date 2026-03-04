package com.project.hanspoon.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SalesTrendDto {
    private List<SalesTrendItem> trend;
    private long totalSales;
    private double growthRate; // 전일 대비 증감률

    @Data
    @Builder
    public static class SalesTrendItem {
        private String date; // "YYYY-MM-DD"
        private long sales;
    }
}
