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
    
    @Column(name = "point_type", nullable = false, length = 20)
    private String pointType;
    
    @Column(nullable = false)
    private Long pointAmount;
    
    @Column(length = 100)
    private String orderId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PointHistory() {}
    
    public PointHistory(Member member, String pointType, Long pointAmount, String orderId) {
        this.member = member;
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.orderId = orderId;
        this.createdAt = LocalDateTime.now();
    }
    
    // 편의를 위한 PointType enum 사용 생성자
    public PointHistory(Member member, PointType pointType, Long pointAmount, String orderId) {
        this(member, pointType.name(), pointAmount, orderId);
    }
    
    public enum PointType {
        EARN,    // 적립
        USE,     // 사용
        REFUND   // 환불 (취소로 인한 포인트 복원)
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public String getPointType() { return pointType; }
    public void setPointType(String pointType) { this.pointType = pointType; }

    public PointType getPointTypeEnum() {
        try {
            return PointType.valueOf(pointType);
        } catch (IllegalArgumentException e) {
            return null; // 알 수 없는 타입의 경우
        }
    }

    public Long getPointAmount() { return pointAmount; }
    public void setPointAmount(Long pointAmount) { this.pointAmount = pointAmount; }

    public Long getPoints() { return pointAmount; } // alias for compatibility

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}