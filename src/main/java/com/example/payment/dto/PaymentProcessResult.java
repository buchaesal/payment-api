package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PaymentProcessResult {
    private String paymentMethod;
    private String orderId;
    private Long amount;
    private String tid;
    private Map<String, Object> pgResult;

    public PaymentProcessResult() {}

    public PaymentProcessResult(String paymentMethod, String orderId, Long amount, String tid, Map<String, Object> pgResult) {
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.amount = amount;
        this.tid = tid;
        this.pgResult = pgResult;
    }
}