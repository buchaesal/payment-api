package com.example.payment.dto;

import com.example.payment.entity.PointHistory;
import java.time.LocalDateTime;

public class PointHistoryResponse {
    
    private Long id;
    private String pointType;
    private Long pointAmount;
    private String orderId;
    private LocalDateTime createdAt;
    
    public PointHistoryResponse() {}
    
    public PointHistoryResponse(Long id, String pointType, Long pointAmount, String orderId, LocalDateTime createdAt) {
        this.id = id;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }
    
    public static PointHistoryResponse from(PointHistory pointHistory) {
        return new PointHistoryResponse(
            pointHistory.getId(),
            pointHistory.getPointType().toString(),
            pointHistory.getPointAmount(),
            pointHistory.getOrderId(),
            pointHistory.getCreatedAt()
        );
    }
    
    public Long getId() { return id; }
    public String getPointType() { return pointType; }
    public Long getPointAmount() { return pointAmount; }
    public String getOrderId() { return orderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setId(Long id) { this.id = id; }
    public void setPointType(String pointType) { this.pointType = pointType; }
    public void setPointAmount(Long pointAmount) { this.pointAmount = pointAmount; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}