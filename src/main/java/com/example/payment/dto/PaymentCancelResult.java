package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PaymentCancelResult {
    private String paymentMethod;
    private String orderId;
    private Long cancelAmount;
    private Map<String, Object> pgResult;
    
    public PaymentCancelResult(String paymentMethod, String orderId, Long cancelAmount, Map<String, Object> pgResult) {
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.cancelAmount = cancelAmount;
        this.pgResult = pgResult;
    }
}