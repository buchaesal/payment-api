package com.example.payment.gateway;

import com.example.payment.client.TossApiClient;
import com.example.payment.dto.PaymentConfirmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TossPaymentGatewayStrategy implements PaymentGatewayStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(TossPaymentGatewayStrategy.class);
    
    @Autowired
    private TossApiClient tossApiClient;
    
    @Override
    public Map<String, Object> processApproval(PaymentConfirmRequest request) {
        
        Map<String, String> authResultMap = request.getAuthResultMap();
        String paymentKey = authResultMap.get("paymentKey"); // 인증 응답의 paymentKey
        String orderId = request.getOrderId(); // 주문 ID
        
        if (paymentKey == null) {
            throw new IllegalArgumentException("토스 승인에 필요한 paymentKey가 없습니다");
        }
        
        if (orderId == null) {
            throw new IllegalArgumentException("토스 승인에 필요한 orderId가 없습니다");
        }

        Long cardAmount = request.getAmount();
        if (cardAmount <= 0) {
            throw new IllegalArgumentException("토스 승인에 필요한 카드 결제 금액이 없습니다");
        }
        
        logger.info("토스 결제 승인 요청: paymentKey={}, amount={}, orderId={}", paymentKey, cardAmount, orderId);

        // v1 API로 승인 요청 (paymentKey, amount, orderId)
        return tossApiClient.requestPaymentApproval(paymentKey, cardAmount, orderId);
    }
    
    @Override
    public Map<String, Object> processCancellation(PaymentConfirmRequest request) {
        logger.info("=== 토스페이먼츠 결제 취소 시작 ===");
        
        // TODO: 실제 토스페이먼츠 취소 API 호출 구현
        // 현재는 임시 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "토스페이먼츠 결제 취소 완료");
        response.put("canceledAt", java.time.LocalDateTime.now().toString());
        
        logger.info("=== 토스페이먼츠 결제 취소 완료 ===");
        return response;
    }
    
    @Override
    public boolean supports(String pgProvider) {
        // TOSS 또는 TOSSPAYMENTS 문자열이 포함되면 토스페이먼츠
        return pgProvider != null && 
               (pgProvider.toUpperCase().contains("TOSS") || 
                pgProvider.toUpperCase().equals("TOSSPAYMENTS"));
    }
}