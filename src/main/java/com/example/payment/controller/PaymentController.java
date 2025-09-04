package com.example.payment.controller;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        
        logger.info("=== 결제 승인 요청 받음 ===");
        logger.info("주문번호: {}, 총 결제금액: {}, 개별결제금액: {}", 
                   request.getOrderId(), 
                   request.getTotalAmount(), 
                   request.getAmount());
        logger.info("회원ID: {}, 사용적립금: {}", request.getMemberId(), request.getUsePoints());
        logger.info("결제항목 수: {}", request.getPaymentItems() != null ? request.getPaymentItems().size() : 0);
        if (request.getPaymentItems() != null) {
            for (int i = 0; i < request.getPaymentItems().size(); i++) {
                var item = request.getPaymentItems().get(i);
                logger.info("결제항목[{}]: {} - {}원", i, item.getPaymentMethod(), item.getAmount());
            }
        }
        
        try {
            Map<String, Object> response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("결제 처리 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 결제 취소 요청 API
     * 복합결제 취소 지원
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@RequestBody PaymentConfirmRequest request) {
        
        logger.info("=== 결제 취소 요청 받음 ===");
        logger.info("주문번호: {}", request.getOrderId());
        
        try {
            Map<String, Object> response = paymentService.cancelPayment(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("결제 취소 처리 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 회원별 결제내역 조회 API
     */
    @GetMapping("/history/{memberId}")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(@PathVariable String memberId) {
        
        logger.info("=== 결제내역 조회 요청 받음 ===");
        logger.info("회원ID: {}", memberId);
        
        try {
            Map<String, Object> response = paymentService.getPaymentHistory(memberId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("결제내역 조회 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "결제내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
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