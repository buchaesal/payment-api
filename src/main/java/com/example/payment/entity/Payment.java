package com.example.payment.entity;

import com.example.payment.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;
    
    @Column(name = "product_name", nullable = true, length = 255)
    private String productName;
    
    @Column(name = "tid", nullable = true, length = 100)
    private String tid; // 결제 거래 ID (PG사에서 발급, 취소 시 필요)
    
    @Column(name = "payment_at", nullable = false)
    private LocalDateTime paymentAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public Payment() {
        // JPA용 기본 생성자
    }
    
    public Payment(String orderId, Member member, PaymentMethod paymentMethod,
                   Long paymentAmount, String productName, String tid) {
        this.orderId = orderId;
        this.member = member;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.productName = productName;
        this.tid = tid;
        this.paymentAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}