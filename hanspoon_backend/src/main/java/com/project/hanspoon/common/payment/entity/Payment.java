package com.project.hanspoon.common.payment.entity;

import com.project.hanspoon.common.payment.constant.PaymentStatus;
import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_id")
    private Long payId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PAID;

    @CreationTimestamp
    @Column(name = "pay_date")
    private LocalDateTime payDate;

    @Column(name = "portone_payment_id")
    private String portOnePaymentId;

    @Column(name = "order_id")
    private Long orderId;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentItem> paymentItems = new ArrayList<>();

    public void addPaymentItem(PaymentItem paymentItem) {
        this.paymentItems.add(paymentItem);
        paymentItem.setPayment(this);
    }

    public boolean cancelPayment() {
        if (this.status == PaymentStatus.CANCELLED) {
            return false;
        }
        this.status = PaymentStatus.CANCELLED;
        return true;
    }
}
