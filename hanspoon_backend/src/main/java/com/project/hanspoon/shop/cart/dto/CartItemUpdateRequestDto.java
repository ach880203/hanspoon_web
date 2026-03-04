package com.project.hanspoon.shop.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemUpdateRequestDto {

    @Min(1)
    private int quantity;
}
