package com.project.hanspoon.admin.service;

import com.project.hanspoon.admin.dto.AdminDashboardSummaryDto;
import com.project.hanspoon.common.payment.repository.PaymentRepository;
import com.project.hanspoon.common.payment.repository.PaymentItemRepository;
import com.project.hanspoon.common.payment.entity.PaymentItem;
import com.project.hanspoon.admin.dto.SalesStatisticsDto;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import com.project.hanspoon.shop.constant.OrderStatus;
import com.project.hanspoon.shop.order.repository.OrderRepository;
import com.project.hanspoon.oneday.inquiry.repository.ClassInquiryRepository;
import com.project.hanspoon.shop.inquiry.repository.InqProductRepository;
import com.project.hanspoon.recipe.repository.RecipeIngRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

        private final PaymentRepository paymentRepository;
        private final PaymentItemRepository paymentItemRepository;
        private final OrderRepository orderRepository;
        private final ClassReservationRepository reservationRepository;
        private final UserRepository userRepository;
        private final ClassInquiryRepository classInquiryRepository;
        private final InqProductRepository inqProductRepository;
        private final RecipeIngRepository recipeIngRepository;

        public AdminDashboardSummaryDto getDashboardSummary() {
                try {
                        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
                        LocalDateTime yesterdayStart = todayStart.minusDays(1);
                        LocalDateTime yesterdayEnd = todayEnd.minusDays(1);

                        // 1. 매출 (Payment 기준, PAID 상태)
                        long todaySales = paymentRepository.findByPayDateBetween(todayStart, todayEnd).stream()
                                        .filter(p -> p.getStatus() == com.project.hanspoon.common.payment.constant.PaymentStatus.PAID)
                                        .mapToLong(p -> p.getTotalPrice() != null ? p.getTotalPrice() : 0)
                                        .sum();

                        long yesterdaySales = paymentRepository.findByPayDateBetween(yesterdayStart, yesterdayEnd)
                                        .stream()
                                        .filter(p -> p.getStatus() == com.project.hanspoon.common.payment.constant.PaymentStatus.PAID)
                                        .mapToLong(p -> p.getTotalPrice() != null ? p.getTotalPrice() : 0)
                                        .sum();

                        // 2. 주문 상태
                        long paymentCompleted = orderRepository.countByStatus(OrderStatus.PAID)
                                        + reservationRepository.countByStatus(ReservationStatus.PAID);
                        long preparing = orderRepository.countByStatus(OrderStatus.CREATED);
                        long shipping = orderRepository.countByStatus(OrderStatus.SHIPPED);
                        long refundRequested = orderRepository.countByStatus(OrderStatus.REFUND_REQUESTED);

                        // 3. 예약 현황
                        long todayReservations = reservationRepository.countByCreatedAtBetweenAndStatusIn(
                                        todayStart, todayEnd,
                                        List.of(ReservationStatus.PAID, ReservationStatus.COMPLETED,
                                                        ReservationStatus.CANCELED,
                                                        ReservationStatus.CANCEL_REQUESTED));

                        long pendingCancel = reservationRepository.countByStatus(ReservationStatus.CANCEL_REQUESTED);

                        long totalCanceled = reservationRepository.countByStatus(ReservationStatus.CANCELED)
                                        + reservationRepository.countByStatus(ReservationStatus.EXPIRED);

                        // 4. CS & 회원
                        long newUsersToday = userRepository.countByCreatedAtBetween(todayStart, todayEnd);
                        long unreadInquiries = classInquiryRepository.countByAnsweredFalse()
                                        + inqProductRepository.countByAnsweredYnFalse()
                                        + recipeIngRepository.countByIsAnsweredFalse();

                        return AdminDashboardSummaryDto.builder()
                                        .sales(AdminDashboardSummaryDto.SalesSummary.builder()
                                                        .todaySales(todaySales)
                                                        .yesterdaySales(yesterdaySales)
                                                        .build())
                                        .orders(AdminDashboardSummaryDto.OrderSummary.builder()
                                                        .paymentCompleted(paymentCompleted)
                                                        .preparing(preparing)
                                                        .shipping(shipping)
                                                        .refundRequested(refundRequested + pendingCancel)
                                                        .build())
                                        .reservations(AdminDashboardSummaryDto.ReservationSummary.builder()
                                                        .todayCount(todayReservations)
                                                        .pendingCancel(pendingCancel)
                                                        .totalCanceled(totalCanceled)
                                                        .build())
                                        .cs(AdminDashboardSummaryDto.CsSummary.builder()
                                                        .newUsersToday(newUsersToday)
                                                        .unreadInquiries(unreadInquiries)
                                                        .build())
                                        .build();
                } catch (Exception e) {
                        throw new RuntimeException("대시보드 요약 생성에 실패했습니다.", e);
                }
        }

        @Transactional(readOnly = true)
        public com.project.hanspoon.admin.dto.SalesTrendDto getSalesTrend(int days) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = now.minusDays(days - 1).toLocalDate().atStartOfDay();

                List<com.project.hanspoon.admin.dto.SalesTrendDto.SalesTrendItem> trend = new java.util.ArrayList<>();
                long totalSales = 0;

                for (int i = 0; i < days; i++) {
                        LocalDateTime targetDate = startDate.plusDays(i);
                        LocalDateTime targetStart = targetDate.toLocalDate().atStartOfDay();
                        LocalDateTime targetEnd = targetDate.toLocalDate().atTime(23, 59, 59);

                        Long dailyAmount = paymentRepository.sumTotalPriceByPayDateBetween(targetStart, targetEnd);
                        long amount = dailyAmount != null ? dailyAmount : 0;

                        trend.add(com.project.hanspoon.admin.dto.SalesTrendDto.SalesTrendItem.builder()
                                        .date(targetDate.toLocalDate().toString())
                                        .sales(amount)
                                        .build());
                        totalSales += amount;
                }

                // 증감률 계산 (오늘 vs 어제)
                long todaySales = trend.get(days - 1).getSales();
                LocalDateTime yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay();
                LocalDateTime yesterdayEnd = now.minusDays(1).toLocalDate().atTime(23, 59, 59);
                Long yesterdaySalesRaw = paymentRepository.sumTotalPriceByPayDateBetween(yesterdayStart, yesterdayEnd);
                long yesterdaySales = yesterdaySalesRaw != null ? yesterdaySalesRaw : 0;

                double growthRate = 0;
                if (yesterdaySales > 0) {
                        growthRate = ((double) (todaySales - yesterdaySales) / yesterdaySales) * 100;
                } else if (todaySales > 0) {
                        growthRate = 100.0;
                }

                return com.project.hanspoon.admin.dto.SalesTrendDto.builder()
                                .trend(trend)
                                .totalSales(totalSales)
                                .growthRate(Math.round(growthRate * 10.0) / 10.0)
                                .build();
        }

        @Transactional(readOnly = true)
        public SalesStatisticsDto getSalesStatistics() {
                // 1. 카테고리 비중 (전체 기간 기준)
                long productSales = paymentRepository.findAll().stream()
                                .filter(p -> p.getStatus() == com.project.hanspoon.common.payment.constant.PaymentStatus.PAID)
                                .filter(p -> p.getPaymentItems().stream().anyMatch(item -> item.getProductId() != null))
                                .mapToLong(p -> p.getTotalPrice() != null ? p.getTotalPrice() : 0)
                                .sum();

                long classSales = paymentRepository.findAll().stream()
                                .filter(p -> p.getStatus() == com.project.hanspoon.common.payment.constant.PaymentStatus.PAID)
                                .filter(p -> p.getPaymentItems().stream().anyMatch(item -> item.getClassId() != null))
                                .mapToLong(p -> p.getTotalPrice() != null ? p.getTotalPrice() : 0)
                                .sum();

                List<SalesStatisticsDto.CategoryRatio> ratios = List.of(
                                SalesStatisticsDto.CategoryRatio.builder().category("상점 상품").value(productSales)
                                                .build(),
                                SalesStatisticsDto.CategoryRatio.builder().category("클래스 예약").value(classSales)
                                                .build());

                // 2. 인기 상품 TOP 5 (수량 기준)
                List<PaymentItem> allItems = paymentItemRepository.findAll();
                java.util.Map<String, Long> countMap = allItems.stream()
                                .filter(item -> item.getItemName() != null)
                                .collect(java.util.stream.Collectors.groupingBy(
                                                PaymentItem::getItemName,
                                                java.util.stream.Collectors.counting()));

                List<SalesStatisticsDto.TopItem> topItems = countMap.entrySet().stream()
                                .map(entry -> SalesStatisticsDto.TopItem.builder()
                                                .name(entry.getKey())
                                                .count(entry.getValue())
                                                .sales(0) // 현재 PaymentItem 에 단가가 없어 0으로 표시하거나 추후 확장
                                                .build())
                                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                                .limit(5)
                                .toList();

                return SalesStatisticsDto.builder()
                                .categoryRatios(ratios)
                                .topItems(topItems)
                                .build();
        }
}
