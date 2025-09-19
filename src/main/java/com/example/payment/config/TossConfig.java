package com.example.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "toss")
public class TossConfig {
    
    /**
     * 토스페이먼츠 API Key
     * - 테스트: sk_test_w5lNQylNqa5lNQe013Nq
     * - 운영: 실제 운영 키로 교체 필요
     */
    private String apiKey;
    
    /**
     * 토스페이먼츠 결제 승인 API URL
     * - v1 API: https://api.tosspayments.com/v1/payments/confirm
     */
    private String executeUrl;

    /**
     * 토스페이먼츠 결제 취소 API URL
     * - v1 API: https://api.tosspayments.com/v1/payments/{paymentKey}/cancel
     * - {paymentKey} 부분은 실제 paymentKey로 치환하여 사용
     */
    private String cancelUrl;
}