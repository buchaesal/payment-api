package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginResponse {
    private String status;
    private String message;
    private MemberResponse member;
    
    public MemberLoginResponse(String status, String message, MemberResponse member) {
        this.status = status;
        this.message = message;
        this.member = member;
    }
}