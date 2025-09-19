package com.example.payment.strategy;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentCancelRequest;
import com.example.payment.dto.PaymentProcessResult;
import com.example.payment.dto.PaymentCancelResult;

public interface PaymentStrategy {

    PaymentProcessResult processPayment(PaymentConfirmRequest request);

    PaymentCancelResult cancelPayment(PaymentCancelRequest request);

    /**
     * 망취소 처리 - 결제 승인 후 로직 실패 시 이미 승인된 결제를 자동 취소
     * @param processResult 성공한 결제 처리 결과
     * @param request 원본 결제 요청 정보
     */
    void performNetCancellation(PaymentProcessResult processResult, PaymentConfirmRequest request);
}