package com.project.hanspoon.shop.cart.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {
    private Long cartId;
    private List<CartItemResponseDto> items;

    private int totalQuantity; // 전체 수량 합
    private int totalPrice;    // 전체 금액 합
}
