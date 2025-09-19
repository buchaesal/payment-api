package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * PG사 응답을 위한 표준화된 DTO
 * 각 PG 전략에서 자신의 응답 키에 맞게 tid를 세팅해서 리턴
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {

    /**
     * 거래 ID - 각 PG사에서 자신의 키로 추출해서 세팅
     * 토스: paymentKey -> tid로 세팅
     * 이니시스: tid -> tid로 세팅
     */
    private String tid;

    /**
     * 주문번호
     */
    private String orderId;

    /**
     * 결제금액
     */
    private Long amount;

    /**
     * 응답 코드 (성공/실패 여부)
     */
    private String responseCode;

    /**
     * 응답 메시지
     */
    private String responseMessage;

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * PG사별 원본 응답 데이터 (필요시 접근용)
     */
    private Map<String, Object> rawResponse;

    /**
     * 승인번호 (카드결제의 경우)
     */
    private String approvalNumber;

    /**
     * 승인일시
     */
    private String approvedAt;
}