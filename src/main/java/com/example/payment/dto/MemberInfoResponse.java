package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInfoResponse {
    private String status;
    private MemberResponse member;
    
    public MemberInfoResponse(String status, MemberResponse member) {
        this.status = status;
        this.member = member;
    }
}