package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthCheckResponse {
    private String status;
    private String message;
    private Long timestamp;
    
    public HealthCheckResponse(String status, String message, Long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}