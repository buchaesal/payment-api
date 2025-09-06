package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.gateway.PaymentGatewayStrategy;
import com.example.payment.gateway.PaymentGatewayStrategyFactory;
import com.example.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CardPaymentStrategy.class);
    
    @Autowired
    private PaymentGatewayStrategyFactory gatewayStrategyFactory;
    
    @Override
    public Map<String, Object> processPayment(PaymentConfirmRequest request) {
        logger.info("=== 카드 결제 승인 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("결제금액: {}원", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        try {
            // authResult 정보 로그 출력
            if (request.getAuthResultMap() != null && !request.getAuthResultMap().isEmpty()) {
                logger.info("--- 카드 인증 결과 정보 ---");
                request.getAuthResultMap().forEach((key, value) -> 
                    logger.info("{}: {}", key, value));
            }
            
            // PG 전략 선택 및 실행
            PaymentGatewayStrategy gatewayStrategy = gatewayStrategyFactory.getStrategy(request.getPgProvider());
            Map<String, Object> gatewayResult = gatewayStrategy.processApproval(request);
            
            // PG 승인 결과를 기반으로 최종 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("status", gatewayResult.get("status"));
            response.put("message", gatewayResult.get("message"));
            response.put("paymentMethod", "CARD");
            response.put("orderId", request.getOrderId());
            response.put("amount", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
            response.put("pgResult", gatewayResult); // PG 승인 결과 전체 포함
            
            logger.info("=== 카드 결제 승인 처리 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("카드 결제 승인 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "카드 결제 승인 실패: " + e.getMessage());
            errorResponse.put("paymentMethod", "CARD");
            errorResponse.put("orderId", request.getOrderId());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> cancelPayment(PaymentConfirmRequest request) {
        logger.info("=== 카드 결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("취소금액: {}원", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        try {
            // PG 전략 선택 및 실행
            PaymentGatewayStrategy gatewayStrategy = gatewayStrategyFactory.getStrategy(request.getPgProvider());
            Map<String, Object> gatewayResult = gatewayStrategy.processCancellation(request);
            
            // PG 취소 결과를 기반으로 최종 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("status", gatewayResult.get("status"));
            response.put("message", gatewayResult.get("message"));
            response.put("paymentMethod", "CARD");
            response.put("orderId", request.getOrderId());
            response.put("cancelAmount", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
            response.put("pgResult", gatewayResult); // PG 취소 결과 전체 포함
            
            logger.info("=== 카드 결제 취소 처리 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("카드 결제 취소 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "카드 결제 취소 실패: " + e.getMessage());
            errorResponse.put("paymentMethod", "CARD");
            errorResponse.put("orderId", request.getOrderId());
            return errorResponse;
        }
    }
}