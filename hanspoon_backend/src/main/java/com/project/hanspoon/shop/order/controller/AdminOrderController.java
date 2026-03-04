package com.project.hanspoon.shop.order.controller;

import com.project.hanspoon.shop.constant.OrderStatus;
import com.project.hanspoon.shop.order.dto.OrderListItemDto;
import com.project.hanspoon.shop.order.dto.OrderResponseDto;
import com.project.hanspoon.shop.order.dto.OrderShipRequestDto;
import com.project.hanspoon.shop.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public ResponseEntity<Page<OrderListItemDto>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.getOrdersForAdmin(status, keyword, page, size));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderForAdmin(orderId));
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponseDto> ship(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderShipRequestDto req
    ) {
        return ResponseEntity.ok(orderService.shipByAdmin(orderId, req.getTrackingNumber()));
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponseDto> deliver(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.deliverByAdmin(orderId));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponseDto> confirm(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.confirmByAdmin(orderId));
    }
}
