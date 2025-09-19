package com.example.payment.dto;

import com.example.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 결제 취소 요청 DTO
 * 취소에 필요한 최소한의 정보만 포함
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequest {

    /**
     * 주문번호 (PG사 취소 API에서 사용)
     */
    private String orderId;

    /**
     * 취소 금액
     */
    private Long amount;

    /**
     * 결제수단 (CARD, POINTS 등)
     */
    private PaymentMethod paymentMethod;

    /**
     * PG 구분코드 (TOSS, INICIS 등)
     */
    private String pgProvider;

    /**
     * 거래 ID (PG사 취소 API에서 사용)
     * - 토스: paymentKey
     * - 이니시스: tid
     */
    private String transactionId;

    /**
     * 회원 ID (적립금 환불 시 사용)
     */
    private String memberId;

    /**
     * 취소 사유 (선택사항)
     */
    private String cancelReason;

    /**
     * PG사별 추가 인증 정보 (필요시)
     */
    private Map<String, String> authData;
}