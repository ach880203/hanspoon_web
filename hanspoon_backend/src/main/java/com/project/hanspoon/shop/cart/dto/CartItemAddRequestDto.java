package com.project.hanspoon.shop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemAddRequestDto {

    @NotNull
    private Long productId;

    @Min(1)
    private int quantity;
}
