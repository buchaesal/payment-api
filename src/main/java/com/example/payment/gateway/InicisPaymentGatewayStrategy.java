package com.example.payment.gateway;

import com.example.payment.client.InicisApiClient;
import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentGatewayResponse;
import com.example.payment.dto.PaymentProcessResult;
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
    public PaymentGatewayResponse processApproval(PaymentConfirmRequest request) {
        logger.info("=== 이니시스 결제 승인 시작 ===");

        Map<String, String> authResultMap = request.getAuthResultMap();
        String resultCode = authResultMap.get("resultCode");

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

        // 실제 이니시스 API 호출
        String orderId = request.getOrderId();
        Map<String, Object> rawResponse = inicisApiClient.requestPaymentApproval(authUrl, authToken, orderId);

        logger.info("이니시스 승인 성공: {}", rawResponse);
        logger.info("=== 이니시스 결제 승인 완료 ===");

        // 이니시스 응답을 표준화된 PaymentGatewayResponse로 변환
        return PaymentGatewayResponse.builder()
                .tid((String) rawResponse.get("tid"))  // 이니시스는 tid 그대로 사용
                .orderId(orderId)
                .amount(request.getAmount())
                .responseCode(resultCode)
                .responseMessage("SUCCESS")
                .success(true)
                .rawResponse(rawResponse)
                .approvalNumber((String) rawResponse.get("approvalNumber"))
                .approvedAt((String) rawResponse.get("approvedAt"))
                .build();
    }
    
    @Override
    public PaymentGatewayResponse processCancellation(PaymentConfirmRequest request) {
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
        Map<String, Object> rawResponse = inicisApiClient.requestPaymentCancel(tid, orderId);

        logger.info("이니시스 취소 성공: {}", rawResponse);
        logger.info("=== 이니시스 결제 취소 완료 ===");

        // 이니시스 취소 응답을 표준화된 PaymentGatewayResponse로 변환
        return PaymentGatewayResponse.builder()
                .tid(tid)  // 이니시스는 tid 그대로 사용
                .orderId(orderId)
                .amount(request.getAmount())
                .responseCode("0000")
                .responseMessage("CANCELED")
                .success(true)
                .rawResponse(rawResponse)
                .build();
    }

    @Override
    public void performNetCancellation(PaymentProcessResult processResult, PaymentConfirmRequest request) {
        logger.warn("=== 이니시스 망취소 시작 ===");
        logger.warn("TID: {}, 금액: {}원", processResult.getTid(), processResult.getAmount());

        try {
            Map<String, Object> authResultMap = request.getAuthResultMap();
            if (authResultMap == null) {
                throw new RuntimeException("이니시스 인증결과가 없어 망취소할 수 없습니다.");
            }

            String netCancelUrl = (String) authResultMap.get("netCancelUrl");
            String authToken = (String) authResultMap.get("authToken");

            if (netCancelUrl == null || authToken == null) {
                throw new RuntimeException("이니시스 망취소에 필요한 정보가 부족합니다. (netCancelUrl, authToken)");
            }

            // 이니시스 전용 망취소 API 호출
            Map<String, Object> cancelResult = inicisApiClient.requestNetCancel(
                netCancelUrl,
                authToken,
                request.getOrderId()
            );
            logger.warn("이니시스 망취소 성공: {}", cancelResult);
        } catch (Exception e) {
            logger.error("이니시스 망취소 실패: {}", e.getMessage());
            throw new RuntimeException("이니시스 망취소 실패: " + e.getMessage(), e);
        }

        logger.warn("=== 이니시스 망취소 완료 ===");
    }

    @Override
    public boolean supports(String pgProvider) {
        // INICIS 문자열이 포함되면 이니시스
        return pgProvider != null && pgProvider.toUpperCase().contains("INICIS");
    }
}