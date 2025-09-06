package com.example.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "toss")
public class TossConfig {
    
    private String apiKey = "sk_test_w5lNQylNqa5lNQe013Nq";  // 테스트용 API Key
    private String executeUrl = "https://pay.toss.im/api/v2/execute";  // 승인 API URL
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getExecuteUrl() {
        return executeUrl;
    }
    
    public void setExecuteUrl(String executeUrl) {
        this.executeUrl = executeUrl;
    }
}