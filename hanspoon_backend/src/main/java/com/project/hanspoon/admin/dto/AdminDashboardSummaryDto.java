package com.project.hanspoon.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AdminDashboardSummaryDto {
    private SalesSummary sales;
    private OrderSummary orders;
    private ReservationSummary reservations;
    private CsSummary cs;

    @Data
    @Builder
    public static class SalesSummary {
        private long todaySales;
        private long yesterdaySales;
    }

    @Data
    @Builder
    public static class OrderSummary {
        private long paymentCompleted; // 결제완료 (배송준비 전)
        private long preparing; // 상품준비중
        private long shipping; // 배송중
        private long refundRequested; // 환불요청 (미처리)
    }

    @Data
    @Builder
    public static class ReservationSummary {
        private long todayCount;
        private long pendingCancel;
        private long totalCanceled;
    }

    @Data
    @Builder
    public static class CsSummary {
        private long unreadInquiries; // 미답변 문의
        private long newUsersToday; // 신규 가입
    }
}
