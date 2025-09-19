package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentCancelRequest;
import com.example.payment.dto.PaymentProcessResult;
import com.example.payment.dto.PaymentCancelResult;
import com.example.payment.dto.PaymentGatewayResponse;
import com.example.payment.gateway.PaymentGatewayStrategy;
import com.example.payment.gateway.PaymentGatewayStrategyFactory;
import com.example.payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CardPaymentStrategy.class);
    private final PaymentGatewayStrategyFactory gatewayStrategyFactory;
    
    @Override
    public PaymentProcessResult processPayment(PaymentConfirmRequest request) {

        // PG 전략 선택 및 실행 (각 PG사에서 tid를 알맞게 세팅해서 리턴)
        PaymentGatewayStrategy gatewayStrategy = gatewayStrategyFactory.getStrategy(request.getPgProvider());
        PaymentGatewayResponse gatewayResponse = gatewayStrategy.processApproval(request);

        // 각 PG사에서 이미 tid를 세팅해서 왔으므로 바로 사용
        String tid = gatewayResponse.getTid();
        Long amount = request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount();

        logger.info("카드 결제 승인 완료 - PG: {}, TID: {}, 요청금액: {}, 응답금액: {}",
                   request.getPgProvider(), tid, amount, gatewayResponse.getAmount());

        return new PaymentProcessResult("CARD", request.getOrderId(), amount, tid, gatewayResponse.getRawResponse());
    }
    
    @Override
    public PaymentCancelResult cancelPayment(PaymentCancelRequest request) {
        logger.info("=== 카드 결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("취소금액: {}원", request.getAmount());
        logger.info("거래ID: {}", request.getTransactionId());

        // PaymentCancelRequest를 PaymentConfirmRequest로 변환 (PG 전략 호환성을 위해)
        PaymentConfirmRequest pgRequest = convertToPgRequest(request);

        // PG 전략 선택 및 실행 (각 PG사에서 tid를 알맞게 세팅해서 리턴)
        PaymentGatewayStrategy gatewayStrategy = gatewayStrategyFactory.getStrategy(request.getPgProvider());
        PaymentGatewayResponse gatewayResponse = gatewayStrategy.processCancellation(pgRequest);

        // 각 PG사에서 이미 tid를 세팅해서 왔으므로 바로 사용
        String cancelTid = gatewayResponse.getTid();

        logger.info("=== 카드 결제 취소 처리 완료 - PG: {}, 취소 TID: {}, 취소금액: {}원 ===",
                   request.getPgProvider(), cancelTid, request.getAmount());

        return new PaymentCancelResult("CARD", request.getOrderId(), request.getAmount(), gatewayResponse.getRawResponse());
    }

    /**
     * PaymentCancelRequest를 PG 전략에서 사용할 PaymentConfirmRequest로 변환
     */
    private PaymentConfirmRequest convertToPgRequest(PaymentCancelRequest cancelRequest) {
        PaymentConfirmRequest pgRequest = new PaymentConfirmRequest();
        pgRequest.setOrderId(cancelRequest.getOrderId());
        pgRequest.setAmount(cancelRequest.getAmount());
        pgRequest.setPgProvider(cancelRequest.getPgProvider());

        // 거래 ID를 authResultMap에 설정
        if (cancelRequest.getTransactionId() != null) {
            Map<String, String> authResultMap = new HashMap<>();
            authResultMap.put("tid", cancelRequest.getTransactionId());
            pgRequest.setAuthResultMap(authResultMap);
        }

        // 추가 인증 데이터가 있으면 병합
        if (cancelRequest.getAuthData() != null) {
            if (pgRequest.getAuthResultMap() == null) {
                pgRequest.setAuthResultMap(new HashMap<>());
            }
            pgRequest.getAuthResultMap().putAll(cancelRequest.getAuthData());
        }

        return pgRequest;
    }
}