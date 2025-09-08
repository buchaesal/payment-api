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
    private LocalDateTime paymentAt;
    private String productName;
    private String memberId;

    public static PaymentDto from(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(payment.getId());
        paymentDto.setOrderId(payment.getOrderId());
        paymentDto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        paymentDto.setPaymentAmount(payment.getPaymentAmount());
        paymentDto.setPaymentAt(payment.getPaymentAt());
        paymentDto.setProductName(payment.getProductName());
        paymentDto.setMemberId(payment.getMember().getMemberId());
        return paymentDto;
    }
}