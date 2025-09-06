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
        logger.info("=== 토스페이먼츠 결제 승인 시작 ===");
        
        Map<String, String> authResultMap = request.getAuthResultMap();
        String payToken = authResultMap.get("paymentKey"); // 인증 응답의 paymentKey가 payToken
        String orderId = authResultMap.get("orderId");
        String amount = authResultMap.get("amount");
        
        logger.info("토스 승인 파라미터 - PayToken: {}, OrderId: {}, Amount: {}", payToken, orderId, amount);
        
        try {
            if (payToken == null) {
                logger.error("토스 승인에 필요한 payToken이 없습니다");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "토스 승인에 필요한 payToken이 없습니다");
                return errorResponse;
            }
            
            // 실제 토스 API 호출
            Map<String, Object> apiResult = tossApiClient.requestPaymentApproval(payToken);
            
            // API 호출 결과 처리
            Map<String, Object> response = new HashMap<>();
            if ("SUCCESS".equals(apiResult.get("status")) || 
                (apiResult.get("httpStatus") != null && (Integer) apiResult.get("httpStatus") == 200)) {
                
                response.put("status", "SUCCESS");
                response.put("message", "토스페이먼츠 결제 승인 완료");
                response.put("payToken", payToken);
                response.put("orderId", orderId);
                response.put("amount", amount);
                response.put("approvedAt", java.time.LocalDateTime.now().toString());
                response.put("method", "CARD");
                response.put("pgProvider", "TOSS");
                response.put("apiResult", apiResult); // 전체 API 응답 포함
                
                logger.info("토스 승인 성공: {}", apiResult);
            } else {
                response.put("status", "FAILED");
                response.put("message", "토스 승인 실패: " + apiResult.get("message"));
                response.put("payToken", payToken);
                response.put("orderId", orderId);
                response.put("amount", amount);
                response.put("pgProvider", "TOSS");
                response.put("apiResult", apiResult); // 실패 응답도 포함
                
                logger.error("토스 승인 실패: {}", apiResult);
            }
            
            logger.info("=== 토스페이먼츠 결제 승인 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("토스페이먼츠 승인 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "토스페이먼츠 승인 실패: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> processCancellation(PaymentConfirmRequest request) {
        logger.info("=== 토스페이먼츠 결제 취소 시작 ===");
        
        try {
            // TODO: 실제 토스페이먼츠 취소 API 호출 구현
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "토스페이먼츠 결제 취소 완료");
            response.put("canceledAt", java.time.LocalDateTime.now().toString());
            
            logger.info("=== 토스페이먼츠 결제 취소 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("토스페이먼츠 취소 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "토스페이먼츠 취소 실패: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public boolean supports(String pgProvider) {
        // TOSS 또는 TOSSPAYMENTS 문자열이 포함되면 토스페이먼츠
        return pgProvider != null && 
               (pgProvider.toUpperCase().contains("TOSS") || 
                pgProvider.toUpperCase().equals("TOSSPAYMENTS"));
    }
}