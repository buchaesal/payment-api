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
    private String payType;
    private String pgProvider;
    private Long paymentAmount;
    private LocalDateTime paymentAt;
    private String productName;
    private String memberId;
    
    // 병합된 결제 정보
    private Boolean isCancelled;
    private LocalDateTime cancelledAt;

    public static PaymentDto from(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(payment.getId());
        paymentDto.setOrderId(payment.getOrderId());
        paymentDto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        paymentDto.setPayType(payment.getPayType() != null ? payment.getPayType().name() : null);
        paymentDto.setPgProvider(payment.getPgProvider());
        paymentDto.setPaymentAmount(payment.getPaymentAmount());
        paymentDto.setPaymentAt(payment.getPaymentAt());
        paymentDto.setProductName(payment.getProductName());
        paymentDto.setMemberId(payment.getMember().getMemberId());
        paymentDto.setIsCancelled(false); // 기본값
        return paymentDto;
    }
}