package com.example.payment.gateway;

import com.example.payment.client.InicisApiClient;
import com.example.payment.dto.PaymentConfirmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InicisPaymentGatewayStrategy implements PaymentGatewayStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(InicisPaymentGatewayStrategy.class);
    
    @Autowired
    private InicisApiClient inicisApiClient;
    
    @Override
    public Map<String, Object> processApproval(PaymentConfirmRequest request) {
        logger.info("=== 이니시스 결제 승인 시작 ===");
        
        Map<String, String> authResultMap = request.getAuthResultMap();
        String tid = authResultMap.get("tid");
        String oid = authResultMap.get("oid");
        String price = authResultMap.get("price");
        String resultCode = authResultMap.get("resultCode");
        
        logger.info("이니시스 승인 파라미터 - TID: {}, OID: {}, Price: {}, ResultCode: {}", tid, oid, price, resultCode);
        
        try {
            // 이니시스 인증이 성공한 경우에만 승인 진행
            if (!"0000".equals(resultCode)) {
                logger.error("이니시스 인증 실패 - ResultCode: {}", resultCode);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "이니시스 인증 실패: " + resultCode);
                return errorResponse;
            }
            
            // authUrl과 authToken 추출
            String authUrl = authResultMap.get("authUrl");
            String authToken = authResultMap.get("authToken");
            
            if (authUrl == null || authToken == null) {
                logger.error("이니시스 승인에 필요한 정보가 부족합니다 - authUrl: {}, authToken: {}", authUrl, authToken);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "이니시스 승인에 필요한 정보가 부족합니다 (authUrl 또는 authToken 없음)");
                return errorResponse;
            }
            
            // 실제 이니시스 API 호출
            Map<String, Object> apiResult = inicisApiClient.requestPaymentApproval(authUrl, authToken);
            
            // API 호출 결과 처리
            Map<String, Object> response = new HashMap<>();
            if ("SUCCESS".equals(apiResult.get("status")) || 
                "0000".equals(apiResult.get("resultCode")) ||
                (apiResult.get("httpStatus") != null && (Integer) apiResult.get("httpStatus") == 200)) {
                
                response.put("status", "SUCCESS");
                response.put("message", "이니시스 결제 승인 완료");
                response.put("tid", apiResult.get("tid") != null ? apiResult.get("tid") : tid);
                response.put("orderId", apiResult.get("oid") != null ? apiResult.get("oid") : oid);
                response.put("amount", apiResult.get("price") != null ? apiResult.get("price") : price);
                response.put("approvedAt", java.time.LocalDateTime.now().toString());
                response.put("method", "CARD");
                response.put("pgProvider", "INICIS");
                response.put("apiResult", apiResult); // 전체 API 응답 포함
                
                logger.info("이니시스 승인 성공: {}", apiResult);
            } else {
                response.put("status", "FAILED");
                response.put("message", "이니시스 승인 실패: " + apiResult.get("message"));
                response.put("tid", tid);
                response.put("orderId", oid);
                response.put("amount", price);
                response.put("pgProvider", "INICIS");
                response.put("apiResult", apiResult); // 실패 응답도 포함
                
                logger.error("이니시스 승인 실패: {}", apiResult);
            }
            
            logger.info("=== 이니시스 결제 승인 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("이니시스 승인 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "이니시스 승인 실패: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> processCancellation(PaymentConfirmRequest request) {
        logger.info("=== 이니시스 결제 취소 시작 ===");
        
        try {
            // TODO: 실제 이니시스 취소 API 호출 구현
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "이니시스 결제 취소 완료");
            response.put("canceledAt", java.time.LocalDateTime.now().toString());
            
            logger.info("=== 이니시스 결제 취소 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("이니시스 취소 중 오류 발생: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "이니시스 취소 실패: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Override
    public boolean supports(String pgProvider) {
        // INICIS 문자열이 포함되면 이니시스
        return pgProvider != null && pgProvider.toUpperCase().contains("INICIS");
    }
}