package com.example.payment.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class PaymentConfirmRequest {
    
    // 주문 정보
    private String customerName;
    private String customerEmail; 
    private String customerPhone;
    private String productName;
    private Integer quantity;
    
    // 회원 정보 및 적립금 사용
    private String memberId;
    private Long usePoints;

    private Map<String, String> authResultMap;

}