package com.example.payment.dto;

public class MemberLoginRequest {
    
    private String memberId;
    
    public MemberLoginRequest() {}
    
    public MemberLoginRequest(String memberId) {
        this.memberId = memberId;
    }
    
    public String getMemberId() { return memberId; }
    
    public void setMemberId(String memberId) { this.memberId = memberId; }
}