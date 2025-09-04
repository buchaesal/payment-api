package com.example.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
public class PointHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType pointType;
    
    @Column(nullable = false)
    private Long pointAmount;
    
    @Column(length = 100)
    private String orderId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    protected PointHistory() {}
    
    public PointHistory(Member member, PointType pointType, Long pointAmount, String orderId) {
        this.member = member;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.orderId = orderId;
        this.createdAt = LocalDateTime.now();
    }
    
    public enum PointType {
        EARN,    // 적립
        USE      // 사용
    }
    
    public Long getId() { return id; }
    public Member getMember() { return member; }
    public PointType getPointType() { return pointType; }
    public Long getPointAmount() { return pointAmount; }
    public String getOrderId() { return orderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}