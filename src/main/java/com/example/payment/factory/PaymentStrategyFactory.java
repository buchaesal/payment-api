package com.example.payment.factory;

import com.example.payment.enums.PaymentMethod;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.impl.CardPaymentStrategy;
import com.example.payment.strategy.impl.PointsPaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyFactory {
    
    @Autowired
    private CardPaymentStrategy cardPaymentStrategy;
    
    @Autowired
    private PointsPaymentStrategy pointsPaymentStrategy;
    
    public PaymentStrategy getStrategy(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제수단이 지정되지 않았습니다.");
        }
        
        switch (paymentMethod) {
            case CARD:
                return cardPaymentStrategy;
            case POINTS:
                return pointsPaymentStrategy;
            default:
                throw new IllegalArgumentException("지원하지 않는 결제수단입니다: " + paymentMethod);
        }
    }
}