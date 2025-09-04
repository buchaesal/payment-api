package com.example.payment.strategy;

import com.example.payment.dto.PaymentConfirmRequest;
import java.util.Map;

public interface PaymentStrategy {
    
    Map<String, Object> processPayment(PaymentConfirmRequest request);
    
    Map<String, Object> cancelPayment(PaymentConfirmRequest request);
}