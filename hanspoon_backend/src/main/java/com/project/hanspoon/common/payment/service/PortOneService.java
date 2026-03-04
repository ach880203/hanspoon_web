package com.project.hanspoon.common.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hanspoon.common.config.PortOneConfig;
import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.payment.constant.PaymentStatus;
import com.project.hanspoon.common.payment.dto.PortOneDto;
import com.project.hanspoon.common.payment.entity.Payment;
import com.project.hanspoon.common.payment.entity.PaymentItem;
import com.project.hanspoon.common.payment.repository.PaymentRepository;
import com.project.hanspoon.common.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortOneService {

    private final WebClient portOneWebClient;
    private final PortOneConfig portOneConfig;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    private final com.project.hanspoon.oneday.clazz.repository.ClassSessionRepository classSessionRepository;
    private final com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository classReservationRepository;
    private final com.project.hanspoon.oneday.coupon.repository.ClassUserCouponRepository userCouponRepository;
    private final com.project.hanspoon.shop.product.repository.ProductRepository productRepository;
    private final com.project.hanspoon.shop.order.repository.OrderRepository orderRepository;
    private final com.project.hanspoon.shop.order.service.OrderService orderService;
    private final com.project.hanspoon.mypage.service.PointService pointService;

    @Transactional
    public PortOneDto.PaymentResult verifyAndSavePayment(
            User user,
            PortOneDto.PaymentVerifyRequest request) {

        String paymentId = request.getPaymentId();
        Long orderIdLong = null;
        if (request.getOrderId() != null) {
            try {
                orderIdLong = Long.parseLong(request.getOrderId());
            } catch (NumberFormatException e) {
                log.warn("올바르지 않은 주문 ID 형식: {}", request.getOrderId());
            }
        }

        try {
            PortOneDto.PortOnePaymentResponse portOnePayment = getPaymentFromPortOne(paymentId);
            if (portOnePayment == null) {
                return PortOneDto.PaymentResult.builder()
                        .success(false)
                        .message("포트원에서 결제 정보를 조회할 수 없습니다.")
                        .build();
            }

            if (portOnePayment.getAmount() == null) {
                log.error("결제 정보 조회 성공했으나 금액 정보가 없음 (paymentId={})", paymentId);
                return PortOneDto.PaymentResult.builder()
                        .success(false)
                        .message("결제 금액 정보를 확인할 수 없습니다.")
                        .build();
            }

            int expectedTotalBeforeDiscount = 0;
            if (request.getProductId() != null) {
                var product = productRepository.findById(request.getProductId())
                        .orElseThrow(() -> new BusinessException("상품 정보를 찾을 수 없습니다."));
                expectedTotalBeforeDiscount = product.getPrice() * request.getQuantity();
            } else if (request.getClassId() != null) {
                var session = classSessionRepository.findById(request.getClassId())
                        .orElseThrow(() -> new BusinessException(
                                "클래스 세션 정보를 찾을 수 없습니다. (ID: " + request.getClassId() + ")"));
                expectedTotalBeforeDiscount = session.getPrice() * request.getQuantity();
            } else if (orderIdLong != null) {
                var order = orderRepository.findById(orderIdLong)
                        .orElseThrow(() -> new BusinessException("주문 정보를 찾을 수 없습니다."));
                expectedTotalBeforeDiscount = order.getTotalPrice();
            }

            if (expectedTotalBeforeDiscount > 0 && request.getAmount() != expectedTotalBeforeDiscount) {
                log.error("사용자 금액 조작 감지: 요청={}, DB 계산={}", request.getAmount(), expectedTotalBeforeDiscount);
                return PortOneDto.PaymentResult.builder()
                        .success(false)
                        .message("결제 금액이 올바르지 않습니다. (조작 의심)")
                        .build();
            }

            Integer paidAmount = portOnePayment.getAmount().getTotal();

            int discountAmount = 0;
            com.project.hanspoon.oneday.coupon.entity.ClassUserCoupon userCoupon = null;

            if (request.getUserCouponId() != null) {
                userCoupon = userCouponRepository.findById(request.getUserCouponId())
                        .orElseThrow(() -> new BusinessException("쿠폰 정보를 찾을 수 없습니다."));

                if (!userCoupon.isUsable(java.time.LocalDateTime.now())) {
                    throw new BusinessException("사용할 수 없는 쿠폰입니다.");
                }

                if (!userCoupon.getUserId().equals(user.getUserId())) {
                    throw new BusinessException("본인 쿠폰만 사용할 수 있습니다.");
                }

                var coupon = userCoupon.getCoupon();
                if (coupon.getDiscountType() == com.project.hanspoon.oneday.coupon.domain.DiscountType.FIXED) {
                    discountAmount = coupon.getDiscountValue();
                } else {
                    discountAmount = (int) (request.getAmount() * (coupon.getDiscountValue() / 100.0));
                }
            }

            int usedPoints = request.getUsedPoints() != null ? request.getUsedPoints() : 0;
            int expectedPaidAmount = (request.getAmount() - discountAmount) - usedPoints;

            if (paidAmount == null || !paidAmount.equals(expectedPaidAmount)) {
                log.error("결제 금액 불일치: 기대={}, 실제={}", expectedPaidAmount, paidAmount);
                return PortOneDto.PaymentResult.builder()
                        .success(false)
                        .message("결제 금액이 일치하지 않습니다. (할인/포인트 적용 확인 필요)")
                        .build();
            }

            if (!"PAID".equals(portOnePayment.getStatus())) {
                return PortOneDto.PaymentResult.builder()
                        .success(false)
                        .message("결제가 완료되지 않았습니다. 상태: " + portOnePayment.getStatus())
                        .build();
            }

            Payment payment = Payment.builder()
                    .user(user)
                    .totalPrice(paidAmount)
                    .status(PaymentStatus.PAID)
                    .portOnePaymentId(paymentId)
                    .orderId(orderIdLong)
                    .payDate(java.time.LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            PaymentItem paymentItem;
            String itemName = portOnePayment.getOrderName() != null ? portOnePayment.getOrderName() : "결제 상품";
            if (request.getProductId() != null) {
                paymentItem = PaymentItem.createForProduct(request.getProductId(), itemName, request.getQuantity());
                savedPayment.addPaymentItem(paymentItem);
            } else if (request.getClassId() != null || request.getReservationId() != null) {
                if (request.getClassId() == null) {
                    throw new BusinessException("클래스 결제에는 세션 ID(classId)가 필요합니다.");
                }

                paymentItem = PaymentItem.createForClass(request.getClassId(), itemName, request.getQuantity());
                savedPayment.addPaymentItem(paymentItem);

                if (request.getReservationId() != null) {
                    com.project.hanspoon.oneday.reservation.entity.ClassReservation reservation = classReservationRepository
                            .findById(request.getReservationId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "예약 정보를 찾을 수 없습니다: " + request.getReservationId()));

                    if (reservation
                            .getStatus() != com.project.hanspoon.oneday.reservation.domain.ReservationStatus.HOLD) {
                        throw new IllegalArgumentException("결제 가능한 예약 상태가 아닙니다: " + reservation.getStatus());
                    }

                    reservation.markPaid(java.time.LocalDateTime.now());
                    reservation.linkPayment(savedPayment.getPayId());
                    classReservationRepository.save(reservation);
                    log.info("기존 예약 확정 및 결제 연동 완료: reservationId={}, payId={}", reservation.getId(),
                            savedPayment.getPayId());
                } else {
                    com.project.hanspoon.oneday.clazz.entity.ClassSession session = classSessionRepository
                            .findById(request.getClassId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "해당 클래스 세션을 찾을 수 없습니다: " + request.getClassId()));

                    com.project.hanspoon.oneday.reservation.entity.ClassReservation reservation = com.project.hanspoon.oneday.reservation.entity.ClassReservation
                            .builder()
                            .session(session)
                            .user(user)
                            .status(com.project.hanspoon.oneday.reservation.domain.ReservationStatus.PAID)
                            .holdExpiredAt(java.time.LocalDateTime.now().plusHours(1))
                            .build();

                    reservation.markPaid(java.time.LocalDateTime.now());
                    reservation.linkPayment(savedPayment.getPayId());
                    classReservationRepository.save(reservation);
                    log.info("클래스 예약 자동 생성 및 결제 연동 완료: userId={}, sessionId={}, payId={}",
                            user.getUserId(), request.getClassId(), savedPayment.getPayId());
                }
            } else if (orderIdLong != null) {
                var order = orderRepository.findById(orderIdLong)
                        .orElseThrow(() -> new BusinessException(
                                "주문 정보를 찾을 수 없습니다. (ID: " + request.getOrderId() + ")"));

                // Order 의 OrderItem 목록에서 PaymentItem 생성 (실제 상품명 스냅샷 저장)
                for (com.project.hanspoon.shop.order.entity.OrderItem oi : order.getItems()) {
                    PaymentItem pi = PaymentItem.createForProduct(
                            oi.getProductId(),
                            oi.getProductName(),
                            oi.getQuantity());
                    savedPayment.addPaymentItem(pi);
                }

                // Use unified order payment completion flow to keep stock/cart handling consistent.
                orderService.completeOrderPaymentBySystem(orderIdLong);
                log.info("상품 주문 결제 완료 처리 완료: orderId={}, payId={}", order.getId(), savedPayment.getPayId());
            } else {
                throw new BusinessException("결제 대상 정보가 없습니다.");
            }

            if (userCoupon != null) {
                userCoupon.markUsed(java.time.LocalDateTime.now());
                log.info("쿠폰 사용 처리 완료: userCouponId={}, userId={}", userCoupon.getId(), user.getUserId());
            }

            if (usedPoints > 0) {
                pointService.usePoints(user.getUserId(), usedPoints, "상품 결제 사용: " + portOnePayment.getOrderName(),
                        savedPayment.getPayId());
                log.info("포인트 차감 완료: usedPoints={}, userId={}", usedPoints, user.getUserId());
            }

            log.info("결제 완료 및 저장 성공: paymentId={}, amount={}, userId={}",
                    paymentId, paidAmount, user.getUserId());

            return PortOneDto.PaymentResult.builder()
                    .success(true)
                    .message("결제가 완료되었습니다.")
                    .payId(savedPayment.getPayId())
                    .paymentId(paymentId)
                    .amount(paidAmount)
                    .build();

        } catch (Exception e) {
            log.error("결제 검증 및 저장 실패: {}", e.getMessage(), e);
            return PortOneDto.PaymentResult.builder()
                    .success(false)
                    .message("결제 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    private PortOneDto.PortOnePaymentResponse getPaymentFromPortOne(String paymentId) {
        try {
            String responseBody = portOneWebClient.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .header("Authorization", "PortOne " + portOneConfig.getApiSecret())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody != null) {
                try {
                    PortOneDto.PortOnePaymentWrapper wrapper = objectMapper.readValue(responseBody,
                            PortOneDto.PortOnePaymentWrapper.class);
                    if (wrapper.getPayment() != null) {
                        return wrapper.getPayment();
                    }
                } catch (Exception ignored) {
                }

                try {
                    return objectMapper.readValue(responseBody, PortOneDto.PortOnePaymentResponse.class);
                } catch (Exception e) {
                    log.error("JSON 파싱 실패", e);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("포트원 결제 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public PortOneDto.PaymentResult cancelPayment(Long payId, String reason) {
        Payment payment = paymentRepository.findById(payId)
                .orElseThrow(() -> new BusinessException("결제 정보를 찾을 수 없습니다."));

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 결제입니다.");
        }

        String paymentId = payment.getPortOnePaymentId();
        if (paymentId == null || paymentId.isBlank()) {
            log.warn("결제 ID(paymentId)가 없어 자동 환불이 불가합니다. payId={}", payId);
            payment.setStatus(PaymentStatus.CANCELLED);
            applyOrderRefundStatus(payment, reason);
            return PortOneDto.PaymentResult.builder()
                    .success(true)
                    .message("결제 ID가 없어 DB 상태만 취소로 변경되었습니다. (수동 환불 필요)")
                    .build();
        }

        try {
            PortOneCancelRequest cancelRequest = new PortOneCancelRequest(reason);

            portOneWebClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .header("Authorization", "PortOne " + portOneConfig.getApiSecret())
                    .bodyValue(cancelRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            payment.setStatus(PaymentStatus.CANCELLED);
            applyOrderRefundStatus(payment, reason);
            log.info("포트원 외부 환불 성공: paymentId={}, reason={}", paymentId, reason);

            return PortOneDto.PaymentResult.builder()
                    .success(true)
                    .message("결제 취소 및 환불이 완료되었습니다.")
                    .payId(payId)
                    .build();

        } catch (Exception e) {
            log.error("포트원 환불 요청 에러 발생: {}", e.getMessage());
            throw new BusinessException("포트원 환불 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private record PortOneCancelRequest(String reason) {
    }

    private void applyOrderRefundStatus(Payment payment, String reason) {
        if (payment.getOrderId() == null) {
            return;
        }
        try {
            orderService.completeRefundByAdmin(payment.getOrderId(), reason);
        } catch (Exception e) {
            log.error("환불 완료 후 주문 상태 업데이트 실패: orderId={}, payId={}, error={}",
                    payment.getOrderId(), payment.getPayId(), e.getMessage());
        }
    }

    public String generateOrderId() {
        return "ORDER_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}
