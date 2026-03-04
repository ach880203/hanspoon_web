package com.project.hanspoon.shop.shipping.entity;

import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "receiver_name", nullable = false, length = 30)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(nullable = false, length = 255)
    private String address1;

    @Column(length = 255)
    private String address2;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(String label, String receiverName, String receiverPhone,
                       String zipCode, String address1, String address2) {
        if (label != null && !label.isBlank()) this.label = label;
        if (receiverName != null && !receiverName.isBlank()) this.receiverName = receiverName;
        if (receiverPhone != null && !receiverPhone.isBlank()) this.receiverPhone = receiverPhone;
        if (zipCode != null) this.zipCode = zipCode;
        if (address1 != null && !address1.isBlank()) this.address1 = address1;
        if (address2 != null) this.address2 = address2;
    }

    public void setDefault(boolean v) {
        this.isDefault = v;
    }
}