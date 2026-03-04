package com.project.hanspoon.shop.order.controller;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.shop.order.dto.OrderCancelRequestDto;
import com.project.hanspoon.shop.order.dto.OrderCreateMyRequestDto;
import com.project.hanspoon.shop.order.dto.OrderListItemDto;
import com.project.hanspoon.shop.order.dto.OrderPayRequestDto;
import com.project.hanspoon.shop.order.dto.OrderResponseDto;
import com.project.hanspoon.shop.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/me")
    public ResponseEntity<OrderResponseDto> createMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid OrderCreateMyRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        OrderResponseDto created = orderService.createOrderFromMyCart(userId, req);
        return ResponseEntity.created(URI.create("/api/orders/me/" + created.getOrderId())).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderListItemDto>> listMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) com.project.hanspoon.shop.constant.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = requireUserId(userDetails);

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        return ResponseEntity.ok(orderService.getMyOrdersWithFilters(userId, start, end, status, page, size));
    }

    @GetMapping("/me/{orderId}")
    public ResponseEntity<OrderResponseDto> getMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(orderService.getMyOrder(userId, orderId));
    }

    @PostMapping("/me/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderCancelRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        String reason = (req == null) ? null : req.getReason();
        return ResponseEntity.ok(orderService.cancelMyOrder(userId, orderId, reason));
    }

    @PostMapping("/me/{orderId}/pay")
    public ResponseEntity<OrderResponseDto> payMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderPayRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        String method = (req == null) ? null : req.getPayMethod();
        return ResponseEntity.ok(orderService.payMyOrder(userId, orderId, method));
    }

    @PostMapping("/me/{orderId}/deliver")
    public ResponseEntity<OrderResponseDto> deliverMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(orderService.deliverMyOrder(userId, orderId));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userDetails.getUser().getUserId();
    }
}