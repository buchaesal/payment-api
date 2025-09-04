package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CardPaymentStrategy.class);
    
    @Override
    public Map<String, Object> processPayment(PaymentConfirmRequest request) {
        logger.info("=== 카드 결제 승인 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("결제금액: {}원", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        // authResult 정보 로그 출력
        if (request.getAuthResultMap() != null && !request.getAuthResultMap().isEmpty()) {
            logger.info("--- 카드 인증 결과 정보 ---");
            request.getAuthResultMap().forEach((key, value) -> 
                logger.info("{}: {}", key, value));
        }
        
        // TODO: 실제 카드 결제 승인 로직 구현
        // - PG사 API 호출
        // - 결제 승인 처리
        // - 결제 결과 저장
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "카드 결제가 성공적으로 승인되었습니다.");
        response.put("paymentMethod", "CARD");
        response.put("orderId", request.getOrderId());
        response.put("amount", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        logger.info("=== 카드 결제 승인 처리 완료 ===");
        return response;
    }
    
    @Override
    public Map<String, Object> cancelPayment(PaymentConfirmRequest request) {
        logger.info("=== 카드 결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("취소금액: {}원", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        // TODO: 실제 카드 결제 취소 로직 구현
        // - PG사 취소 API 호출
        // - 결제 취소 처리
        // - 결제 취소 결과 저장
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "카드 결제가 성공적으로 취소되었습니다.");
        response.put("paymentMethod", "CARD");
        response.put("orderId", request.getOrderId());
        response.put("cancelAmount", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        logger.info("=== 카드 결제 취소 처리 완료 ===");
        return response;
    }
}