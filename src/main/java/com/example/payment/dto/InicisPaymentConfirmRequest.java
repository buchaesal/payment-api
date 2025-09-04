package com.example.payment.dto;

public class InicisPaymentConfirmRequest {
    
    // 이니시스 인증 응답값 (PC)
    private String tid;              // 거래ID
    private String payMethod;        // 결제수단
    private String mid;              // 상점ID
    private String authToken;        // 인증토큰
    private String authUrl;          // 승인URL
    private String netCancel;        // 망취소 여부
    private String checkAckUrl;      // 체크 URL
    private String timestamp;        // 타임스탬프
    private String signature;        // 전자서명
    private String verification;     // 검증데이터
    private Integer price;           // 결제금액
    
    // 주문 정보
    private String customerName;
    private String customerEmail; 
    private String customerPhone;
    private String productName;
    private Integer quantity;
    
    // 회원 정보 및 적립금 사용
    private String memberId;
    private Long usePoints;
    
    public InicisPaymentConfirmRequest() {}
    
    // Getter 메서드들
    public String getTid() { return tid; }
    public String getPayMethod() { return payMethod; }
    public String getMid() { return mid; }
    public String getAuthToken() { return authToken; }
    public String getAuthUrl() { return authUrl; }
    public String getNetCancel() { return netCancel; }
    public String getCheckAckUrl() { return checkAckUrl; }
    public String getTimestamp() { return timestamp; }
    public String getSignature() { return signature; }
    public String getVerification() { return verification; }
    public Integer getPrice() { return price; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public String getMemberId() { return memberId; }
    public Long getUsePoints() { return usePoints; }
    
    // Setter 메서드들
    public void setTid(String tid) { this.tid = tid; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public void setMid(String mid) { this.mid = mid; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }
    public void setNetCancel(String netCancel) { this.netCancel = netCancel; }
    public void setCheckAckUrl(String checkAckUrl) { this.checkAckUrl = checkAckUrl; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setVerification(String verification) { this.verification = verification; }
    public void setPrice(Integer price) { this.price = price; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setUsePoints(Long usePoints) { this.usePoints = usePoints; }
    
    @Override
    public String toString() {
        return "InicisPaymentConfirmRequest{" +
                "tid='" + tid + '\'' +
                ", payMethod='" + payMethod + '\'' +
                ", mid='" + mid + '\'' +
                ", authToken='" + authToken + '\'' +
                ", authUrl='" + authUrl + '\'' +
                ", netCancel='" + netCancel + '\'' +
                ", price=" + price +
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