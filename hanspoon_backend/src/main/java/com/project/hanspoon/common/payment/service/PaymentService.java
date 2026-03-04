package com.project.hanspoon.common.payment.service;

import com.project.hanspoon.common.payment.constant.PaymentStatus;
import com.project.hanspoon.common.payment.entity.PaymentItem;
import com.project.hanspoon.common.payment.entity.Payment;
import com.project.hanspoon.common.payment.dto.PaymentDto;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.payment.repository.PaymentItemRepository;
import com.project.hanspoon.common.payment.repository.PaymentRepository;
import com.project.hanspoon.shop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final PortOneService portOneService;
    private final OrderRepository orderRepository;

    /**
     * 결제 생성
     */
    public Payment createPayment(User user, int totalPrice) {
        Payment payment = Payment.builder()
                .user(user)
                .totalPrice(totalPrice)
                .status(PaymentStatus.PAID)
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * 상품 결제 생성
     */
    public PaymentDto createPaymentForProduct(User user, Long productId,
            int price, int quantity) {
        int totalPrice = price * quantity;
        Payment payment = createPayment(user, totalPrice);

        PaymentItem paymentItem = PaymentItem.createForProduct(productId, null, quantity);
        payment.addPaymentItem(paymentItem);

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentDto.from(savedPayment);
    }

    /**
     * 클래스 결제 생성
     */
    public PaymentDto createPaymentForClass(User user, Long classId, int price,
            int quantity) {
        int totalPrice = price * quantity;
        Payment payment = createPayment(user, totalPrice);

        PaymentItem paymentItem = PaymentItem.createForClass(classId, null, quantity);
        payment.addPaymentItem(paymentItem);

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentDto.from(savedPayment);
    }

    /**
     * 결제 취소
     */
    public Payment cancelPayment(Long payId) {
        // 포트원 취소 및 DB 업데이트
        portOneService.cancelPayment(payId, "관리자 취소");

        return paymentRepository.findById(payId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }

    /**
     * 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentDto getPayment(Long payId) {
        Payment payment = paymentRepository.findById(payId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        return enrichOrderStatus(PaymentDto.from(payment));
    }

    /**
     * 사용자의 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentHistory(Long userId, Pageable pageable) {
        return paymentRepository.findByUserUserIdOrderByPayDateDesc(userId, pageable)
                .map(PaymentDto::from)
                .map(this::enrichOrderStatus);
    }

    /**
     * 결제 상태별 조회
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * 전체 결제 내역 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(PaymentDto::from)
                .map(this::enrichOrderStatus);
    }

    private PaymentDto enrichOrderStatus(PaymentDto dto) {
        if (dto.getOrderId() == null) {
            return dto;
        }
        orderRepository.findById(dto.getOrderId())
                .ifPresent(order -> dto.setOrderStatus(order.getStatus().name()));
        return dto;
    }
}
