package com.example.payment.dto;

public class MemberSignupRequest {
    
    private String memberId;
    private String name;
    private String email;
    private String phone;
    
    public MemberSignupRequest() {}
    
    public MemberSignupRequest(String memberId, String name, String email, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}