package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSignupResponse {
    private String status;
    private String message;
    private MemberResponse member;
    
    public MemberSignupResponse(String status, String message, MemberResponse member) {
        this.status = status;
        this.message = message;
        this.member = member;
    }
}