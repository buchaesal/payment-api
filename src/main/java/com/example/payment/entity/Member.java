package com.example.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String memberId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(nullable = false, precision = 10, scale = 0)
    private Long points = 0L;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    protected Member() {}
    
    public Member(String memberId, String name, String email, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.points = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addPoints(Long points) {
        this.points += points;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void usePoints(Long points) {
        if (this.points < points) {
            throw new IllegalArgumentException("보유 적립금이 부족합니다.");
        }
        this.points -= points;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Long getPoints() { return points; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}