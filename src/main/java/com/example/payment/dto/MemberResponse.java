package com.example.payment.dto;

public class MemberResponse {
    
    private Long id;
    private String memberId;
    private String name;
    private String email;
    private String phone;
    private Long points;
    
    public MemberResponse() {}
    
    public MemberResponse(Long id, String memberId, String name, String email, String phone, Long points) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.points = points;
    }
    
    public Long getId() { return id; }
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Long getPoints() { return points; }
    
    public void setId(Long id) { this.id = id; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPoints(Long points) { this.points = points; }
}