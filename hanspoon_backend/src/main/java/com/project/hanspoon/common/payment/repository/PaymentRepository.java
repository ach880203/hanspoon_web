package com.project.hanspoon.common.payment.repository;

import com.project.hanspoon.common.payment.constant.PaymentStatus;
import com.project.hanspoon.common.payment.entity.Payment;
import com.project.hanspoon.common.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    @Query("select p from Payment p where p.user.userId = :userId order by p.payDate desc")
    List<Payment> findByUserId(@Param("userId") Long userId);

    List<Payment> findByUser_UserId(Long userId);
    
    // 사용자의 결제 내역 (페이징)
    Page<Payment> findByUserOrderByPayDateDesc(User user, Pageable pageable);
    
    // 사용자의 결제 내역 (userId로)
    Page<Payment> findByUserUserIdOrderByPayDateDesc(Long userId, Pageable pageable);
    
    // 상태별 결제 내역
    List<Payment> findByStatus(PaymentStatus status);
    
    // 사용자 + 상태별 결제 내역
    List<Payment> findByUserAndStatus(User user, PaymentStatus status);
    
    // 기간별 결제 내역
    List<Payment> findByPayDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 일자별 결산(매출 집계)용 합계 조회입니다.
     * 초보자 참고:
     * - 결제 상태가 이후에 바뀌어도(예: 예약 상태 COMPLETED/CANCELED 전환) 결제 시점 매출은 보존하기 위해
     *   pay_date 기준으로 집계합니다.
     */
    @Query("""
            select coalesce(sum(p.totalPrice), 0)
            from Payment p
            where p.payDate >= :start
              and p.payDate <= :end
            """)
    Long sumTotalPriceByPayDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select p
            from Payment p
            join p.paymentItems item
            where p.user.userId = :userId
              and item.classId = :sessionId
              and p.status = :status
            order by p.payDate desc
            """)
    List<Payment> findClassPaymentsByUserAndSessionAndStatus(
            @Param("userId") Long userId,
            @Param("sessionId") Long sessionId,
            @Param("status") PaymentStatus status
    );
}
