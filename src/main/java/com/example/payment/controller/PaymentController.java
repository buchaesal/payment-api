package com.example.payment.controller;

import com.example.payment.dto.*;
import com.example.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // CORS 설정
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 결제 승인 요청 API
     * 복합결제를 지원하여 다양한 결제수단 조합 처리
     */
    @PostMapping("/confirm")
    public PaymentConfirmResponse confirmPayment(@RequestBody PaymentConfirmRequest request) {

        try {
            return paymentService.processPayment(request);
        } catch (Exception e) {
            logger.error("결제 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 결제 취소 요청 API
     * 복합결제 취소 지원
     */
    @PostMapping("/cancel")
    public Object cancelPayment(@RequestBody PaymentConfirmRequest request) {
        
        logger.info("=== 결제 취소 요청 받음 ===");
        logger.info("주문번호: {}", request.getOrderId());
        
        try {
            return paymentService.cancelPayment(request);
        } catch (Exception e) {
            logger.error("결제 취소 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 회원별 결제내역 조회 API
     */
    @GetMapping("/history/{memberId}")
    public PaymentHistoryResponse getPaymentHistory(@PathVariable String memberId) {
        
        logger.info("=== 결제내역 조회 요청 받음 ===");
        logger.info("회원ID: {}", memberId);
        
        try {
            return paymentService.getPaymentHistory(memberId);
        } catch (Exception e) {
            logger.error("결제내역 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("결제내역 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 주문번호로 결제 정보 조회 API
     */
    @GetMapping("/order/{orderId}")
    public PaymentOrderResponse getPaymentByOrderId(@PathVariable String orderId) {
        
        logger.info("=== 주문번호로 결제정보 조회 요청 받음 ===");
        logger.info("주문번호: {}", orderId);
        
        try {
            return paymentService.getPaymentByOrderId(orderId);
        } catch (Exception e) {
            logger.error("주문번호로 결제정보 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("결제정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    public HealthCheckResponse healthCheck() {
        logger.info("헬스체크 요청 처리됨");
        
        return new HealthCheckResponse(
            "OK",
            "토스페이먼츠 Spring Boot API 서버가 정상 작동중입니다.",
            System.currentTimeMillis()
        );
    }

}