package com.example.payment.gateway;

import com.example.payment.client.TossApiClient;
import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentGatewayResponse;
import com.example.payment.dto.PaymentProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TossPaymentGatewayStrategy implements PaymentGatewayStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(TossPaymentGatewayStrategy.class);
    
    @Autowired
    private TossApiClient tossApiClient;
    
    @Override
    public PaymentGatewayResponse processApproval(PaymentConfirmRequest request) {

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
        Map<String, Object> rawResponse = tossApiClient.requestPaymentApproval(paymentKey, cardAmount, orderId);

        // 토스페이먼츠 응답을 표준화된 PaymentGatewayResponse로 변환
        return PaymentGatewayResponse.builder()
                .tid(paymentKey)  // 토스는 paymentKey를 tid로 사용
                .orderId(orderId)
                .amount(cardAmount)
                .responseCode("0000")  // 성공 시 기본 코드
                .responseMessage("SUCCESS")
                .success(true)
                .rawResponse(rawResponse)
                .approvalNumber((String) rawResponse.get("approvalNumber"))
                .approvedAt((String) rawResponse.get("approvedAt"))
                .build();
    }
    
    @Override
    public PaymentGatewayResponse processCancellation(PaymentConfirmRequest request) {
        logger.info("=== 토스페이먼츠 결제 취소 시작 ===");

        // PaymentConfirmRequest에서 필요한 정보 추출
        // 취소 시에는 이미 저장된 Payment의 tid를 사용 (토스의 경우 tid = paymentKey)
        String paymentKey = null;
        if (request.getAuthResultMap() != null) {
            paymentKey = request.getAuthResultMap().get("tid");  // 토스의 경우 tid에 paymentKey가 저장됨
        }

        String orderId = request.getOrderId();
        String cancelReason = "단순변심";  // 고정값으로 설정

        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("토스 취소에 필요한 paymentKey가 없습니다");
        }

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("토스 취소에 필요한 orderId가 없습니다");
        }

        logger.info("토스 결제 취소 요청: paymentKey={}, orderId={}, cancelReason={}", paymentKey, orderId, cancelReason);

        // 실제 토스페이먼츠 취소 API 호출
        Map<String, Object> rawResponse = tossApiClient.requestPaymentCancellation(paymentKey, cancelReason, orderId);

        logger.info("=== 토스페이먼츠 결제 취소 완료 ===");

        // 토스페이먼츠 취소 응답을 표준화된 PaymentGatewayResponse로 변환
        return PaymentGatewayResponse.builder()
                .tid(paymentKey)  // 토스는 paymentKey를 tid로 사용
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
        logger.warn("=== 토스페이먼츠 망취소 시작 ===");
        logger.warn("PaymentKey: {}, 금액: {}원", processResult.getTid(), processResult.getAmount());

        try {
            // 토스페이먼츠는 기존 취소 API를 그대로 사용
            Map<String, Object> cancelResult = tossApiClient.requestPaymentCancellation(
                processResult.getTid(), // paymentKey
                "시스템 처리 실패로 인한 자동 망취소",
                request.getOrderId()
            );
            logger.warn("토스페이먼츠 망취소 성공: {}", cancelResult);
        } catch (Exception e) {
            logger.error("토스페이먼츠 망취소 실패: {}", e.getMessage());
            throw new RuntimeException("토스페이먼츠 망취소 실패: " + e.getMessage(), e);
        }

        logger.warn("=== 토스페이먼츠 망취소 완료 ===");
    }

    @Override
    public boolean supports(String pgProvider) {
        // TOSS 또는 TOSSPAYMENTS 문자열이 포함되면 토스페이먼츠
        return pgProvider != null &&
               (pgProvider.toUpperCase().contains("TOSS") ||
                pgProvider.toUpperCase().equals("TOSSPAYMENTS"));
    }
}