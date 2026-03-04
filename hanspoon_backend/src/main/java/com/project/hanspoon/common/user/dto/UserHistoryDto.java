package com.project.hanspoon.common.user.dto;

import com.project.hanspoon.shop.order.dto.OrderResponseDto;
import com.project.hanspoon.oneday.reservation.dto.ClassReservationResponseDto;
import com.project.hanspoon.common.payment.dto.PaymentDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHistoryDto {
    private List<OrderResponseDto> orders;
    private List<ClassReservationResponseDto> reservations;
    private List<PaymentDto> payments;
}
