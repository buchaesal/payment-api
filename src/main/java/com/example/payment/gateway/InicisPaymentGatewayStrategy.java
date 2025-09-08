package com.example.payment.gateway;

import com.example.payment.client.InicisApiClient;
import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InicisPaymentGatewayStrategy implements PaymentGatewayStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(InicisPaymentGatewayStrategy.class);
    
    @Autowired
    private InicisApiClient inicisApiClient;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Override
    public Map<String, Object> processApproval(PaymentConfirmRequest request) {
        logger.info("=== 이니시스 결제 승인 시작 ===");
        
        Map<String, String> authResultMap = request.getAuthResultMap();
        String tid = authResultMap.get("tid");
        String oid = authResultMap.get("oid");
        String price = authResultMap.get("price");
        String resultCode = authResultMap.get("resultCode");
        
        logger.info("이니시스 승인 파라미터 - TID: {}, OID: {}, Price: {}, ResultCode: {}", tid, oid, price, resultCode);
        
        // 이니시스 인증이 성공한 경우에만 승인 진행
        if (!"0000".equals(resultCode)) {
            logger.error("이니시스 인증 실패 - ResultCode: {}", resultCode);
            throw new IllegalArgumentException("이니시스 인증 실패: " + resultCode);
        }
        
        // authUrl과 authToken 추출
        String authUrl = authResultMap.get("authUrl");
        String authToken = authResultMap.get("authToken");
        
        if (authUrl == null || authToken == null) {
            logger.error("이니시스 승인에 필요한 정보가 부족합니다 - authUrl: {}, authToken: {}", authUrl, authToken);
            throw new IllegalArgumentException("이니시스 승인에 필요한 정보가 부족합니다 (authUrl 또는 authToken 없음)");
        }
        
        // 실제 이니시스 API 호출 (이미 예외를 던지도록 수정됨)
        String orderId = request.getOrderId(); // 주문 ID 추가
        Map<String, Object> apiResult = inicisApiClient.requestPaymentApproval(authUrl, authToken, orderId);
        
        logger.info("이니시스 승인 성공: {}", apiResult);
        logger.info("=== 이니시스 결제 승인 완료 ===");
        
        // 성공시에는 승인 응답 값만 리턴
        return apiResult;
    }
    
    @Override
    public Map<String, Object> processCancellation(PaymentConfirmRequest request) {
        logger.info("=== 이니시스 결제 취소 시작 ===");
        
        String orderId = request.getOrderId();
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("취소에 필요한 주문 ID가 없습니다");
        }
        
        logger.info("취소 요청 주문 ID: {}", orderId);
        
        String tid = request.getAuthResultMap().get("tid");
        if (tid == null || tid.trim().isEmpty()) {
            throw new IllegalArgumentException("취소에 필요한 TID가 없습니다. 주문 ID: " + orderId);
        }
        
        logger.info("취소 대상 TID: {}", tid);
        
        // 실제 이니시스 취소 API 호출
        Map<String, Object> apiResult = inicisApiClient.requestPaymentCancel(tid, orderId);
        
        logger.info("이니시스 취소 성공: {}", apiResult);
        logger.info("=== 이니시스 결제 취소 완료 ===");
        
        // 성공시에는 취소 응답 값만 리턴
        return apiResult;
    }
    
    @Override
    public boolean supports(String pgProvider) {
        // INICIS 문자열이 포함되면 이니시스
        return pgProvider != null && pgProvider.toUpperCase().contains("INICIS");
    }
}