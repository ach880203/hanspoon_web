package com.project.hanspoon.shop.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDto {
    private Long itemId;

    private Long productId;
    private String name;
    private int price;
    private int stock;
    private int quantity;

    private int lineTotal; // price * quantity
    private String thumbnailUrl;
}
