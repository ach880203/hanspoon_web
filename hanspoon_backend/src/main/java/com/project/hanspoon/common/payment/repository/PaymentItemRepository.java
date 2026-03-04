package com.project.hanspoon.common.payment.repository;

import com.project.hanspoon.common.payment.entity.PaymentItem;
import com.project.hanspoon.common.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
    
    // 결제별 주문 항목
    List<PaymentItem> findByPayment(Payment payment);
    
    // 결제 ID로 주문 항목 조회
    List<PaymentItem> findByPaymentPayId(Long payId);
    
    // 상품별 주문 내역
    List<PaymentItem> findByProductId(Long productId);
    
    // 클래스별 주문 내역
    List<PaymentItem> findByClassId(Long classId);
}
