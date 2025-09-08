package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaymentOrderResponse {
    private String status;
    private String message;
    private String orderId;
    private String productName;
    private Long totalAmount;
    private Integer paymentCount;
    private List<PaymentDto> paymentList;
    
    public PaymentOrderResponse(String status, String message, String orderId, 
                                String productName, Long totalAmount, 
                                Integer paymentCount, List<PaymentDto> paymentDtoList) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.paymentCount = paymentCount;
        this.paymentList = paymentDtoList;
    }
}