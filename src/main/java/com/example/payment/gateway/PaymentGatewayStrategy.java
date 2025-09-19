package com.example.payment.gateway;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentGatewayResponse;
import com.example.payment.dto.PaymentProcessResult;

public interface PaymentGatewayStrategy {

    /**
     * PG사별 결제 승인 처리
     * @param request 결제 승인 요청 정보
     * @return 표준화된 승인 결과 (각 PG사에서 tid 등을 알맞게 세팅)
     */
    PaymentGatewayResponse processApproval(PaymentConfirmRequest request);

    /**
     * PG사별 결제 취소 처리
     * @param request 결제 취소 요청 정보
     * @return 표준화된 취소 결과
     */
    PaymentGatewayResponse processCancellation(PaymentConfirmRequest request);

    /**
     * PG사별 망취소 처리 - 결제 승인 후 로직 실패 시 자동 취소
     * @param processResult 성공한 결제 처리 결과
     * @param request 원본 결제 요청 정보
     */
    void performNetCancellation(PaymentProcessResult processResult, PaymentConfirmRequest request);

    /**
     * 해당 PG사 지원 여부 확인
     * @param pgProvider PG 구분코드 (TOSS, INICIS 등)
     * @return 지원 여부
     */
    boolean supports(String pgProvider);
}