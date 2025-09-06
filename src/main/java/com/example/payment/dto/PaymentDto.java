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
    
    // Lombok이 제대로 작동하지 않을 경우를 위한 수동 setter 메서드들
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public void setPaymentAmount(Long paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public void setPaymentAt(LocalDateTime paymentAt) {
        this.paymentAt = paymentAt;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}