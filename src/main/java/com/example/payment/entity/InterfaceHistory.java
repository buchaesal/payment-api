package com.example.payment.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interface_histories")
@Getter
@Setter
@NoArgsConstructor
public class InterfaceHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "interface_type", nullable = false, length = 50)
    private String interfaceType; // TOSS, INICIS 등
    
    @Column(name = "api_name", nullable = false, length = 100)
    private String apiName; // confirm, cancel 등
    
    @Column(name = "request_url", length = 500)
    private String requestUrl; // 요청 URL
    
    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson; // 요청 JSON
    
    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson; // 응답 JSON
    
    @Column(name = "response_code", length = 10)
    private String responseCode; // 응답 코드 (성공: 0000, 실패: 기타)
    
    @Column(name = "http_status")
    private Integer httpStatus; // HTTP 상태 코드
    
    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime; // 요청 시간
    
    @Column(name = "response_time")
    private LocalDateTime responseTime; // 응답 시간
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs; // 처리 시간 (밀리초)
    
    @Column(name = "order_id", length = 100)
    private String orderId; // 주문 ID (연관성을 위해)
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 에러 메시지
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 요청 시작 시 호출하는 정적 메서드
     */
    public static InterfaceHistory createRequest(String interfaceType, String apiName, String requestUrl, String requestJson, String orderId) {
        InterfaceHistory history = new InterfaceHistory();
        history.interfaceType = interfaceType;
        history.apiName = apiName;
        history.requestUrl = requestUrl;
        history.requestJson = requestJson;
        history.orderId = orderId;
        history.requestTime = LocalDateTime.now();
        return history;
    }
    
    /**
     * 응답 완료 시 호출하는 메서드
     */
    public void completeResponse(String responseJson, String responseCode, Integer httpStatus, String errorMessage) {
        this.responseJson = responseJson;
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
        this.responseTime = LocalDateTime.now();
        
        if (this.requestTime != null && this.responseTime != null) {
            this.processingTimeMs = java.time.Duration.between(this.requestTime, this.responseTime).toMillis();
        }
    }
}