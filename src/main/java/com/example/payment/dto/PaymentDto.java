package com.example.payment.dto;

import com.example.payment.entity.Payment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDto {
    private Long id;
    private String orderId;
    private String paymentMethod;
    private Long paymentAmount;
    private String paymentStatus;
    private LocalDateTime paymentAt;
    private String productName;
    private String memberId;
    
    public PaymentDto() {
    }
    
    public PaymentDto(Long id, String orderId, String paymentMethod, 
                     Long paymentAmount, String paymentStatus,
                     LocalDateTime paymentAt, String productName, String memberId) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.paymentAt = paymentAt;
        this.productName = productName;
        this.memberId = memberId;
    }

    public static PaymentDto from(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(payment.getId());
        paymentDto.setOrderId(payment.getOrderId());
        paymentDto.setPaymentAmount(payment.getPaymentAmount());
        paymentDto.setPaymentStatus(payment.getPaymentStatus());
        paymentDto.setPaymentAt(payment.getPaymentAt());
        paymentDto.setProductName(payment.getProductName());
        return paymentDto;
    }
}