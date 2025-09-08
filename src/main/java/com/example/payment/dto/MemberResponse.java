package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    
    private Long id;
    private String memberId;
    private String name;
    private String email;
    private String phone;
    private Long points;

}