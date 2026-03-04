package com.project.hanspoon.shop.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderShipRequestDto {
    @NotBlank(message = "송장번호는 필수입니다.")
    private String trackingNumber;
}
