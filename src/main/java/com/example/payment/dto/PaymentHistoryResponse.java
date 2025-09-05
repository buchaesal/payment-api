package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaymentHistoryResponse {
    private String status;
    private String message;
    private String memberId;
    private Integer paymentCount;
    private List<PaymentDto> paymentList;
    
    public PaymentHistoryResponse(String status, String message, String memberId, 
                                  Integer paymentCount, List<PaymentDto> paymentList) {
        this.status = status;
        this.message = message;
        this.memberId = memberId;
        this.paymentCount = paymentCount;
        this.paymentList = paymentList;
    }
}