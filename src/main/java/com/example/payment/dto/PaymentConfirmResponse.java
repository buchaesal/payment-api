package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PaymentConfirmResponse {
    private String status;
    private String message;
    private String orderId;
    private Long totalAmount;
    private Long processedAmount;
    private List<Map<String, Object>> paymentResults;
    private Integer paymentCount;
    
    public PaymentConfirmResponse(String status, String message, String orderId, 
                                  Long totalAmount, Long processedAmount, 
                                  List<Map<String, Object>> paymentResults, 
                                  Integer paymentCount) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.processedAmount = processedAmount;
        this.paymentResults = paymentResults;
        this.paymentCount = paymentCount;
    }
}