package com.example.payment.gateway;

import com.example.payment.dto.PaymentConfirmRequest;

import java.util.Map;

public interface PaymentGatewayStrategy {
    
    /**
     * PG사별 결제 승인 처리
     * @param request 결제 승인 요청 정보
     * @return 승인 결과
     */
    Map<String, Object> processApproval(PaymentConfirmRequest request);
    
    /**
     * PG사별 결제 취소 처리
     * @param request 결제 취소 요청 정보
     * @return 취소 결과
     */
    Map<String, Object> processCancellation(PaymentConfirmRequest request);
    
    /**
     * 해당 PG사 지원 여부 확인
     * @param pgProvider PG 구분코드 (TOSS, INICIS 등)
     * @return 지원 여부
     */
    boolean supports(String pgProvider);
}