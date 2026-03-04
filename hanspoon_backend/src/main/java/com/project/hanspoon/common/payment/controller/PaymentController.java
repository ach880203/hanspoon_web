package com.project.hanspoon.common.payment.controller;

import com.project.hanspoon.common.config.PortOneConfig;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.payment.dto.PaymentDto;
import com.project.hanspoon.common.payment.dto.PortOneDto;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.common.payment.service.PaymentService;
import com.project.hanspoon.common.payment.service.PortOneService;
import com.project.hanspoon.common.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 REST API Controller
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PortOneService portOneService;
    private final UserService userService;
    private final PortOneConfig portOneConfig;

    /**
     * 결제 준비 정보 조회
     * GET /api/payment/checkout-info
     */
    @GetMapping("/checkout-info")
    public ResponseEntity<ApiResponse<PortOneDto.CheckoutInfo>> getCheckoutInfo(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long classId,
            @RequestParam int price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam String orderName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        User user = userService.findById(userDetails.getUserId());

        PortOneDto.CheckoutInfo checkoutInfo = PortOneDto.CheckoutInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .productId(productId)
                .classId(classId)
                .price(price)
                .quantity(quantity)
                .totalAmount(price * quantity)
                .orderName(orderName)
                .orderId(portOneService.generateOrderId())
                .storeId(portOneConfig.getStoreId())
                .channelKeyKakao(portOneConfig.getChannelKey().getKakao())
                .channelKeyToss(portOneConfig.getChannelKey().getToss())
                .channelKeyTossPayments(portOneConfig.getChannelKey().getTossPayments())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(checkoutInfo));
    }

    /**
     * 결제 사전 준비 (주문번호 생성)
     * POST /api/payment/prepare
     */
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PortOneDto.PrepareResponse>> preparePayment(
            @RequestBody PortOneDto.PrepareRequest prepareRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 로그인한 사용자라면 사용자 정보 검증 로직 추가 가능
        // 현재는 주문번호 생성만 처리
        String merchantUid = portOneService.generateOrderId();

        PortOneDto.PrepareResponse response = PortOneDto.PrepareResponse.builder()
                .merchantUid(merchantUid)
                .amount(prepareRequest.getAmount())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 포트원 결제 검증
     * POST /api/payment/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PortOneDto.PaymentResult>> verifyPayment(
            @RequestBody PortOneDto.PaymentVerifyRequest verifyRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        System.out.println(
                "결제 검증 요청 시작: paymentId=" + verifyRequest.getPaymentId() + ", orderId=" + verifyRequest.getOrderId());
        System.out.println("userDetails 정보: " + userDetails);

        if (userDetails == null) {
            System.out.println("userDetails가 null입니다. 401 에러를 반환합니다.");
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        System.out.println("사용자 인증 완료: userId=" + userDetails.getUserId());
        User user = userService.findById(userDetails.getUserId());

        PortOneDto.PaymentResult result = portOneService.verifyAndSavePayment(user, verifyRequest);

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.ok("결제가 완료되었습니다.", result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(result.getMessage()));
        }
    }

    /**
     * 상품 결제 생성
     * POST /api/payment/product
     */
    @PostMapping("/product")
    public ResponseEntity<ApiResponse<PaymentDto>> createPaymentForProduct(
            @RequestBody PortOneDto.PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        User user = userService.findById(userDetails.getUserId());
        PaymentDto paymentDto = paymentService.createPaymentForProduct(
                user, request.getProductId(), request.getTotalAmount(), request.getQuantity());

        return ResponseEntity.ok(ApiResponse.ok("결제가 생성되었습니다.", paymentDto));
    }

    /**
     * 클래스 결제 생성
     * POST /api/payment/class
     */
    @PostMapping("/class")
    public ResponseEntity<ApiResponse<PaymentDto>> createPaymentForClass(
            @RequestBody PortOneDto.PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        User user = userService.findById(userDetails.getUserId());
        PaymentDto paymentDto = paymentService.createPaymentForClass(
                user, request.getClassId(), request.getTotalAmount(), request.getQuantity());

        return ResponseEntity.ok(ApiResponse.ok("결제가 생성되었습니다.", paymentDto));
    }

    /**
     * 결제 취소
     * POST /api/payment/{payId}/cancel
     */
    @PostMapping("/{payId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(@PathVariable Long payId) {
        try {
            paymentService.cancelPayment(payId);
            return ResponseEntity.ok(ApiResponse.ok("결제가 취소되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 결제 내역 조회
     * GET /api/payment/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<PaymentDto>>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "payDate", direction = Sort.Direction.DESC) Pageable pageable) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        Page<PaymentDto> dtoPage = paymentService.getPaymentHistory(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(dtoPage)));
    }

    /**
     * 결제 상세 조회
     * GET /api/payment/{payId}
     */
    @GetMapping("/{payId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentDetail(@PathVariable Long payId) {
        PaymentDto paymentDto = paymentService.getPayment(payId);
        return ResponseEntity.ok(ApiResponse.ok(paymentDto));
    }

    /**
     * 포트원 설정 정보 조회 (프론트엔드용)
     * GET /api/payment/config
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<PortOneDto.ConfigInfo>> getPortOneConfig() {
        PortOneDto.ConfigInfo configInfo = PortOneDto.ConfigInfo.builder()
                .storeId(portOneConfig.getStoreId())
                .channelKeyKakao(
                        portOneConfig.getChannelKey() != null ? portOneConfig.getChannelKey().getKakao() : null)
                .channelKeyToss(portOneConfig.getChannelKey() != null ? portOneConfig.getChannelKey().getToss() : null)
                .channelKeyTossPayments(
                        portOneConfig.getChannelKey() != null ? portOneConfig.getChannelKey().getTossPayments() : null)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(configInfo));
    }
}
