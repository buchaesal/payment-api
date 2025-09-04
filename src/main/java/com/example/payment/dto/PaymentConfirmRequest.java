package com.example.payment.dto;

public class PaymentConfirmRequest {
    
    // 토스페이먼츠 인증 응답값
    private String paymentKey;
    private String orderId;
    private Integer amount;
    
    // 주문 정보
    private String customerName;
    private String customerEmail; 
    private String customerPhone;
    private String productName;
    private Integer quantity;
    
    // 회원 정보 및 적립금 사용
    private String memberId;
    private Long usePoints;
    
    // 기본 생성자
    public PaymentConfirmRequest() {}
    
    // 모든 필드를 받는 생성자
    public PaymentConfirmRequest(String paymentKey, String orderId, Integer amount,
                               String customerName, String customerEmail, String customerPhone,
                               String productName, Integer quantity) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.productName = productName;
        this.quantity = quantity;
    }
    
    // Getter 메서드들
    public String getPaymentKey() { return paymentKey; }
    public String getOrderId() { return orderId; }
    public Integer getAmount() { return amount; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public String getMemberId() { return memberId; }
    public Long getUsePoints() { return usePoints; }
    
    // Setter 메서드들
    public void setPaymentKey(String paymentKey) { this.paymentKey = paymentKey; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setUsePoints(Long usePoints) { this.usePoints = usePoints; }
    
    // toString 메서드 (로그 출력용)
    @Override
    public String toString() {
        return "PaymentConfirmRequest{" +
                "paymentKey='" + paymentKey + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", memberId='" + memberId + '\'' +
                ", usePoints=" + usePoints +
                '}';
    }
}