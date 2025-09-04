package com.example.payment.dto;

import com.example.payment.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentItem {
    
    private PaymentMethod paymentMethod;
    private Long amount;
    
    public PaymentItem() {}
    
    public PaymentItem(PaymentMethod paymentMethod, Long amount) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }
    
    // 수동으로 추가한 getter/setter 메서드들 (Lombok 이슈 대응)
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
}