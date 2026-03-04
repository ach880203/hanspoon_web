package com.project.hanspoon.shop.shipping.dto;

import com.project.hanspoon.shop.shipping.entity.ShippingAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class ShippingAddressDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String label;
        @NotBlank private String receiverName;
        @NotBlank private String receiverPhone;
        private String zipCode;
        @NotBlank private String address1;
        private String address2;
        private Boolean isDefault;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String label;
        private String receiverName;
        private String receiverPhone;
        private String zipCode;
        private String address1;
        private String address2;
        private Boolean isDefault;
    }

    @Getter
    @Builder
    public static class Response {
        private Long shippingAddressId;
        private String label;
        private String receiverName;
        private String receiverPhone;
        private String zipCode;
        private String address1;
        private String address2;
        private boolean isDefault;

        public static Response from(ShippingAddress e) {
            return Response.builder()
                    .shippingAddressId(e.getShippingAddressId())
                    .label(e.getLabel())
                    .receiverName(e.getReceiverName())
                    .receiverPhone(e.getReceiverPhone())
                    .zipCode(e.getZipCode())
                    .address1(e.getAddress1())
                    .address2(e.getAddress2())
                    .isDefault(e.isDefault())
                    .build();
        }
    }
}