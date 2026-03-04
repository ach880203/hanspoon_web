package com.project.hanspoon.common.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

public class PortOneDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentRequest {
        private String orderName;
        private Integer totalAmount;
        private String payMethod;
        private Long productId;
        private Long classId;
        private Long reservationId; // 예약 ID 추가
        private Long userCouponId; // 쿠폰 ID 추가
        private Integer usedPoints; // 사용 포인트 추가
        private Integer quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentVerifyRequest {
        private String paymentId;
        private String orderId;
        private Integer amount;
        private Long productId;
        private Long classId;
        private Long reservationId; // 예약 ID 추가
        private Long userCouponId; // 쿠폰 ID 추가
        private Integer usedPoints; // 사용 포인트 추가
        private Integer quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrepareRequest {
        private String itemName;
        private Integer amount;
        private String buyerName;
        private String buyerEmail;
        private String buyerTel;
        private String paymentMethod;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrepareResponse {
        private String merchantUid;
        private Integer amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortOnePaymentWrapper {
        private PortOnePaymentResponse payment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortOnePaymentResponse {
        private String id;
        private String status;
        private String transactionId;
        private String merchantId;
        private String storeId;
        private Method method;
        private Channel channel;
        private Amount amount;
        private String currency;
        private Customer customer;
        private String paidAt;
        private String orderName;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Method {
            private String type;
            private Card card;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Card {
            private String publisher;
            private String issuer;
            private String brand;
            private String type;
            private String bin;
            private String name;
            private String number;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Channel {
            private String id;
            private String key;
            private String name;
            private String pgProvider;
            private String pgMerchantId;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Amount {
            private Integer total;
            private Integer taxFree;
            private Integer vat;
            private Integer supply;
            private Integer discount;
            private Integer paid;
            private Integer cancelled;
            private Integer cancelledTaxFree;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Customer {
            private String id;
            private String name;
            private String email;
            private String phoneNumber;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResult {
        private boolean success;
        private String message;
        private Long payId;
        private String paymentId;
        private Integer amount;
    }

    /**
     * 결제 준비 정보 (프론트엔드용)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutInfo {
        private Long userId;
        private String email;
        private String userName;
        private Long productId;
        private Long classId;
        private Integer price;
        private Integer quantity;
        private Integer totalAmount;
        private String orderName;
        private String orderId;
        private String storeId;
        private String channelKeyKakao;
        private String channelKeyToss;
        private String channelKeyTossPayments;
    }

    /**
     * 포트원 설정 정보 (프론트엔드용)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfigInfo {
        private String storeId;
        private String channelKeyKakao;
        private String channelKeyToss;
        private String channelKeyTossPayments;
    }
}
