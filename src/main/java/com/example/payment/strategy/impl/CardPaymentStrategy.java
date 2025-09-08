package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentProcessResult;
import com.example.payment.dto.PaymentCancelResult;
import com.example.payment.gateway.PaymentGatewayStrategy;
import com.example.payment.gateway.PaymentGatewayStrategyFactory;
import com.example.payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CardPaymentStrategy.class);
    private final PaymentGatewayStrategyFactory gatewayStrategyFactory;
    
    @Override
    public PaymentProcessResult processPayment(PaymentConfirmRequest request) {

        // PG 전략 선택 및 실행 (에러시 예외 던짐)
        PaymentGatewayStrategy gatewayStrategy = gatewayStrategyFactory.getStrategy(request.getPgProvider());
        Map<String, Object> gatewayResult = gatewayStrategy.processApproval(request);
        
        // tid 추출 (PaymentService에서 사용)
        String tid = null;
        Object tidObject = gatewayResult.get("tid");
        if (tidObject != null) {
            tid = tidObject.toString();
        }
        
        Long amount = request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount();
        
        return new PaymentProcessResult("CARD", request.getOrderId(), amount, tid, gatewayResult);
    }
    
    @Override
    public PaymentCancelResult cancelPayment(PaymentConfirmRequest request) {
        logger.info("=== 카드 결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("취소금액: {}원", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        
        // PG 전략 선택 및 실행 (에러시 예외 던짐)
        PaymentGatewayStrategy gatewayStrategy = gatewayStrategyFactory.getStrategy(request.getPgProvider());
        Map<String, Object> gatewayResult = gatewayStrategy.processCancellation(request);
        
        Long cancelAmount = request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount();
        
        logger.info("=== 카드 결제 취소 처리 완료 ===");
        
        return new PaymentCancelResult("CARD", request.getOrderId(), cancelAmount, gatewayResult);
    }
}