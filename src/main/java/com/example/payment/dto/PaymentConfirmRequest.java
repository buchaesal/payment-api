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
    @Deprecated
    private Long amount;
    
    // 회원 정보
    private String memberId;
    private Long usePoints;  // 적립금 사용액

    // 카드 결제 인증결과 (authResult)
    private Map<String, String> authResultMap;

    // 수동으로 추가한 getter/setter 메서드들 (Lombok 이슈 대응)
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    
    public Long getUsePoints() { return usePoints; }
    public void setUsePoints(Long usePoints) { this.usePoints = usePoints; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public List<PaymentItem> getPaymentItems() { return paymentItems; }
    public void setPaymentItems(List<PaymentItem> paymentItems) { this.paymentItems = paymentItems; }
    
    public Map<String, String> getAuthResultMap() { return authResultMap; }
    public void setAuthResultMap(Map<String, String> authResultMap) { this.authResultMap = authResultMap; }
}