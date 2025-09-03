package com.example.payment.controller;

import com.example.payment.dto.PaymentConfirmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // CORS 설정
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    /**
     * 결제 승인 요청 API
     * 파라미터로 받은 주문정보와 인증응답값을 서버 콘솔에 출력
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        
        logger.info("=== 결제 승인 요청 받음 ===");
        logger.info("전체 요청 데이터: {}", request.toString());
        
        // 각 필드별로 상세 로그 출력
        logger.info("--- 토스페이먼츠 인증 응답값 ---");
        logger.info("PaymentKey: {}", request.getPaymentKey());
        logger.info("OrderId: {}", request.getOrderId());
        logger.info("Amount: {}", request.getAmount());
        
        logger.info("--- 주문자 정보 ---");
        logger.info("고객명: {}", request.getCustomerName());
        logger.info("이메일: {}", request.getCustomerEmail());
        logger.info("연락처: {}", request.getCustomerPhone());
        
        logger.info("--- 주문 상품 정보 ---");
        logger.info("상품명: {}", request.getProductName());
        logger.info("수량: {}", request.getQuantity());
        
        logger.info("=== 결제 승인 요청 처리 완료 ===");
        
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "결제 승인 요청이 성공적으로 처리되었습니다.");
        response.put("orderId", request.getOrderId());
        response.put("amount", request.getAmount());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("헬스체크 요청 처리됨");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "토스페이먼츠 Spring Boot API 서버가 정상 작동중입니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}