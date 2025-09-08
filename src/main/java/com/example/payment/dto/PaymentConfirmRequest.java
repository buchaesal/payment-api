package com.example.payment.dto;

import com.example.payment.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PaymentConfirmRequest {
    
    // 주문 정보
    private String orderId;
    private Long totalAmount;  // 전체 결제금액
    private String customerName;
    private String customerEmail; 
    private String customerPhone;
    private String productName;
    private Integer quantity;
    
    // 복합결제 정보 (결제수단별 금액)
    private List<PaymentItem> paymentItems;
    
    // 단일결제 호환성을 위한 필드들 (deprecated, 하위호환용)
    @Deprecated
    private PaymentMethod paymentMethod;
    private Long amount;
    
    // 회원 정보
    private String memberId;
    private Long usePoints;  // 적립금 사용액

    // 카드 결제 인증결과 (authResult)
    private Map<String, String> authResultMap;
    
    // PG 구분코드 (TOSS, INICIS 등)
    private String pgProvider;
}