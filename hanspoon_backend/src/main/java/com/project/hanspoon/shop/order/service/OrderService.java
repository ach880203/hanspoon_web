package com.project.hanspoon.shop.order.service;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.shop.cart.entity.Cart;
import com.project.hanspoon.shop.cart.entity.CartItem;
import com.project.hanspoon.shop.cart.repository.CartItemRepository;
import com.project.hanspoon.shop.cart.repository.CartRepository;
import com.project.hanspoon.shop.constant.OrderStatus;
import com.project.hanspoon.shop.order.dto.OrderCreateMyRequestDto;
import com.project.hanspoon.shop.order.dto.OrderItemResponseDto;
import com.project.hanspoon.shop.order.dto.OrderListItemDto;
import com.project.hanspoon.shop.order.dto.OrderResponseDto;
import com.project.hanspoon.shop.order.entity.Order;
import com.project.hanspoon.shop.order.entity.OrderItem;
import com.project.hanspoon.shop.order.repository.OrderItemRepository;
import com.project.hanspoon.shop.order.repository.OrderRepository;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.product.entity.ProductImage;
import com.project.hanspoon.shop.product.repository.ProductImageRepository;
import com.project.hanspoon.shop.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public OrderResponseDto createOrderFromMyCart(Long userId, OrderCreateMyRequestDto req) {
        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니가 없습니다. userId=" + userId));

        Long cartId = cart.getId();
        List<CartItem> cartItems = cartItemRepository.findByCart_IdOrderByIdDesc(cartId);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "장바구니가 비어있습니다.");
        }

        User userRef = em.getReference(User.class, userId);
        Order order = Order.builder()
                .user(userRef)
                .cartId(cartId)
                .status(OrderStatus.CREATED)
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .address1(req.getAddress1())
                .address2(req.getAddress2())
                .totalPrice(0)
                .build();

        int total = 0;

        for (CartItem ci : cartItems) {
            Long productId = ci.getProduct().getId();
            int qty = ci.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId));

            if (qty <= 0) {
                throw new ResponseStatusException(BAD_REQUEST, "수량이 올바르지 않습니다.");
            }
            if (product.getStock() < qty) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "재고 부족: " + product.getName() + " (재고 " + product.getStock() + ", 요청 " + qty + ")");
            }

            String thumb = productImageRepository.findFirstByProduct_IdAndRepYnTrue(productId)
                    .map(ProductImage::getImgUrl)
                    .orElse(null);

            OrderItem oi = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .orderPrice(product.getPrice())
                    .quantity(qty)
                    .thumbnailUrl(thumb)
                    .build();

            order.addItem(oi);
            total += (product.getPrice() * qty);
        }

        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public OrderResponseDto getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findWithItemsByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));
        return toResponse(order);
    }

    public Page<OrderListItemDto> getMyOrders(Long userId, int page, int size) {
        return getMyOrdersWithFilters(userId, null, null, null, page, size);
    }

    public Page<OrderListItemDto> getMyOrdersWithFilters(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> ordersPage = orderRepository.findWithFilters(userId, startDate, endDate, status, pageable);

        List<Long> orderIds = ordersPage.getContent().stream().map(Order::getId).toList();
        Map<Long, Integer> countMap = new HashMap<>();
        Map<Long, OrderItem> firstItemMap = new HashMap<>();

        if (!orderIds.isEmpty()) {
            List<OrderItem> items = orderItemRepository.findByOrder_IdInOrderByOrder_IdAscIdAsc(orderIds);
            for (OrderItem oi : items) {
                Long oid = oi.getOrder().getId();
                countMap.merge(oid, 1, Integer::sum);
                firstItemMap.putIfAbsent(oid, oi);
            }
        }

        List<OrderListItemDto> dtoList = ordersPage.getContent().stream()
                .map(o -> {
                    OrderItem first = firstItemMap.get(o.getId());
                    return OrderListItemDto.builder()
                            .orderId(o.getId())
                            .status(o.getStatus())
                            .totalPrice(o.getTotalPrice())
                            .createdAt(o.getCreatedAt())
                            .receiverName(o.getReceiverName())
                            .trackingNumber(o.getTrackingNumber())
                            .itemCount(countMap.getOrDefault(o.getId(), 0))
                            .firstItemName(first != null ? first.getProductName() : null)
                            .firstItemThumbnailUrl(first != null ? first.getThumbnailUrl() : null)
                            .build();
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, ordersPage.getTotalElements());
    }

    @Transactional
    public OrderResponseDto cancelMyOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findWithItemsByIdAndUserIdForUpdate(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() == OrderStatus.CANCELED
                || order.getStatus() == OrderStatus.REFUND_REQUESTED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new ResponseStatusException(BAD_REQUEST, "이미 취소/환불된 주문입니다.");
        }

        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.SHIPPED
                && order.getStatus() != OrderStatus.DELIVERED
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(BAD_REQUEST, "취소 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        if (!StringUtils.hasText(reason)) {
            throw new ResponseStatusException(BAD_REQUEST, "환불 사유(reason)는 필수입니다.");
        }

        order.setStatus(OrderStatus.REFUND_REQUESTED);
        order.setRefundReason(reason.trim());
        return toResponse(order);
    }

    @Transactional
    public OrderResponseDto payMyOrder(Long userId, Long orderId, String payMethod) {
        Order order = orderRepository.findWithItemsByIdAndUserIdForUpdate(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() == OrderStatus.CANCELED
                || order.getStatus() == OrderStatus.REFUND_REQUESTED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new ResponseStatusException(BAD_REQUEST, "취소/환불된 주문은 결제할 수 없습니다.");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return toResponse(order);
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(BAD_REQUEST, "결제 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        applyPaymentSuccess(order);
        return toResponse(order);
    }

    @Transactional
    public void completeOrderPaymentBySystem(Long orderId) {
        Order order = orderRepository.findWithItemsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(BAD_REQUEST, "결제 완료 처리 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        applyPaymentSuccess(order);
    }

    @Transactional
    public OrderResponseDto shipMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findWithItemsByIdAndUserIdForUpdate(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() == OrderStatus.CANCELED
                || order.getStatus() == OrderStatus.REFUND_REQUESTED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new ResponseStatusException(BAD_REQUEST, "취소/환불된 주문은 배송 처리할 수 없습니다.");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new ResponseStatusException(BAD_REQUEST, "출고 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        return toResponse(order);
    }

    @Transactional
    public OrderResponseDto deliverMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findWithItemsByIdAndUserIdForUpdate(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new ResponseStatusException(BAD_REQUEST, "배송완료 처리 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        return toResponse(order);
    }

    public Page<OrderListItemDto> getOrdersForAdmin(OrderStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> ordersPage = orderRepository.findAllWithFilters(status, keyword, pageable);

        List<Long> orderIds = ordersPage.getContent().stream().map(Order::getId).toList();
        Map<Long, Integer> countMap = new HashMap<>();
        Map<Long, OrderItem> firstItemMap = new HashMap<>();

        if (!orderIds.isEmpty()) {
            List<OrderItem> items = orderItemRepository.findByOrder_IdInOrderByOrder_IdAscIdAsc(orderIds);
            for (OrderItem oi : items) {
                Long oid = oi.getOrder().getId();
                countMap.merge(oid, 1, Integer::sum);
                firstItemMap.putIfAbsent(oid, oi);
            }
        }

        List<OrderListItemDto> dtoList = ordersPage.getContent().stream()
                .map(o -> {
                    OrderItem first = firstItemMap.get(o.getId());
                    return OrderListItemDto.builder()
                            .orderId(o.getId())
                            .status(o.getStatus())
                            .totalPrice(o.getTotalPrice())
                            .createdAt(o.getCreatedAt())
                            .receiverName(o.getReceiverName())
                            .trackingNumber(o.getTrackingNumber())
                            .itemCount(countMap.getOrDefault(o.getId(), 0))
                            .firstItemName(first != null ? first.getProductName() : null)
                            .firstItemThumbnailUrl(first != null ? first.getThumbnailUrl() : null)
                            .build();
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, ordersPage.getTotalElements());
    }

    @Transactional
    public OrderResponseDto shipByAdmin(Long orderId, String trackingNumber) {
        Order order = orderRepository.findWithItemsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (!StringUtils.hasText(trackingNumber)) {
            throw new ResponseStatusException(BAD_REQUEST, "송장번호는 필수입니다.");
        }

        if (order.getStatus() == OrderStatus.CANCELED
                || order.getStatus() == OrderStatus.REFUND_REQUESTED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new ResponseStatusException(BAD_REQUEST, "취소/환불된 주문은 배송 처리할 수 없습니다.");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new ResponseStatusException(BAD_REQUEST, "출고 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        order.setTrackingNumber(trackingNumber.trim());
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        return toResponse(order);
    }

    @Transactional
    public OrderResponseDto deliverByAdmin(Long orderId) {
        Order order = orderRepository.findWithItemsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new ResponseStatusException(BAD_REQUEST, "배송완료 처리 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        return toResponse(order);
    }

    @Transactional
    public OrderResponseDto confirmByAdmin(Long orderId) {
        Order order = orderRepository.findWithItemsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(BAD_REQUEST, "구매확정 처리 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        return toResponse(order);
    }

    @Transactional
    public OrderResponseDto completeRefundByAdmin(Long orderId, String reason) {
        Order order = orderRepository.findWithItemsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));

        if (order.getStatus() == OrderStatus.REFUNDED) {
            return toResponse(order);
        }

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new ResponseStatusException(BAD_REQUEST, "취소된 주문은 환불 처리할 수 없습니다.");
        }

        if (order.getStatus() != OrderStatus.REFUND_REQUESTED && order.getStatus() != OrderStatus.PAID) {
            throw new ResponseStatusException(BAD_REQUEST, "환불 완료 처리 가능한 상태가 아닙니다. status=" + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                            "주문 상품이 존재하지 않습니다. productId=" + item.getProductId()));
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundedAt(LocalDateTime.now());
        if (StringUtils.hasText(reason)) {
            order.setRefundReason(reason.trim());
        }

        return toResponse(order);
    }

    public OrderResponseDto getOrderForAdmin(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "주문이 없습니다. orderId=" + orderId));
        return toResponse(order);
    }

    private OrderResponseDto toResponse(Order order) {
        List<OrderItemResponseDto> items = order.getItems().stream()
                .map(i -> OrderItemResponseDto.builder()
                        .orderItemId(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .orderPrice(i.getOrderPrice())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getLineTotal())
                        .thumbnailUrl(i.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .confirmedAt(order.getConfirmedAt())
                .trackingNumber(order.getTrackingNumber())
                .refundedAt(order.getRefundedAt())
                .refundReason(order.getRefundReason())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .items(items)
                .build();
    }

    private void applyPaymentSuccess(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                            "주문 상품이 삭제되었거나 존재하지 않습니다. productId=" + item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "재고 부족: " + product.getName() + " (재고 " + product.getStock() + ", 요청 " + item.getQuantity() + ")");
            }

            product.setStock(product.getStock() - item.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        if (order.getCartId() != null) {
            List<CartItem> cartItems = cartItemRepository.findByCart_IdOrderByIdDesc(order.getCartId());
            if (!cartItems.isEmpty()) {
                cartItemRepository.deleteAll(cartItems);
            }
        }
    }
}
