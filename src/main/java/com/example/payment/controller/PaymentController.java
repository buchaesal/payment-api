package com.example.payment.controller;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.InicisPaymentConfirmRequest;
import com.example.payment.service.MemberService;
import com.example.payment.entity.Member;
import com.example.payment.entity.PointHistory;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PointHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // CORS 설정
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    
    /**
     * 결제 승인 요청 API
     * 파라미터로 받은 주문정보와 인증응답값을 서버 콘솔에 출력
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        
        logger.info("=== 결제 승인 요청 받음 ===");
        logger.info("전체 요청 데이터: {}", request.toString());
        
        // 각 필드별로 상세 로그 출력
        logger.info("--- 토스페이먼츠 인증 응답값 ---");
        logger.info("PaymentKey: {}", request.getPaymentKey());
        logger.info("OrderId: {}", request.getOrderId());
        logger.info("Amount: {}", request.getAmount());
        
        logger.info("--- 주문자 정보 ---");
        logger.info("고객명: {}", request.getCustomerName());
        logger.info("이메일: {}", request.getCustomerEmail());
        logger.info("연락처: {}", request.getCustomerPhone());
        
        logger.info("--- 주문 상품 정보 ---");
        logger.info("상품명: {}", request.getProductName());
        logger.info("수량: {}", request.getQuantity());
        
        // 적립금 사용 처리
        Long finalAmount = request.getAmount().longValue();
        String pointMessage = "";
        
        if (request.getMemberId() != null && request.getUsePoints() != null && request.getUsePoints() > 0) {
            logger.info("--- 적립금 사용 정보 ---");
            logger.info("회원 ID: {}", request.getMemberId());
            logger.info("사용 적립금: {}원", request.getUsePoints());
            
            try {
                Member member = memberRepository.findByMemberId(request.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
                
                if (member.getPoints() < request.getUsePoints()) {
                    throw new IllegalArgumentException("보유 적립금이 부족합니다.");
                }
                
                member.usePoints(request.getUsePoints());
                memberRepository.save(member);
                
                PointHistory pointHistory = new PointHistory(
                    member,
                    PointHistory.PointType.USE,
                    request.getUsePoints(),
                    "결제 시 적립금 사용 (주문번호: " + request.getOrderId() + ")"
                );
                pointHistoryRepository.save(pointHistory);
                
                finalAmount = request.getAmount().longValue() - request.getUsePoints();
                pointMessage = String.format(" (적립금 %d원 사용, 실결제금액: %d원)", request.getUsePoints(), finalAmount);
                
                logger.info("적립금 사용 완료 - 잔여 적립금: {}원", member.getPoints());
                
            } catch (Exception e) {
                logger.error("적립금 사용 처리 중 오류 발생: {}", e.getMessage());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "ERROR");
                errorResponse.put("message", "적립금 사용 처리 중 오류가 발생했습니다: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        logger.info("=== 결제 승인 요청 처리 완료 ===");
        
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "결제 승인 요청이 성공적으로 처리되었습니다." + pointMessage);
        response.put("orderId", request.getOrderId());
        response.put("amount", request.getAmount());
        response.put("finalAmount", finalAmount);
        response.put("usePoints", request.getUsePoints() != null ? request.getUsePoints() : 0);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("헬스체크 요청 처리됨");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "토스페이먼츠 Spring Boot API 서버가 정상 작동중입니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이니시스 결제 승인 요청 API
     * 파라미터로 받은 주문정보와 인증응답값을 서버 콘솔에 출력
     */
    @PostMapping("/inicis/confirm")
    public ResponseEntity<Map<String, Object>> confirmInicisPayment(@RequestBody InicisPaymentConfirmRequest request) {
        
        logger.info("=== 이니시스 결제 승인 요청 받음 ===");
        logger.info("전체 요청 데이터: {}", request.toString());
        
        // 각 필드별로 상세 로그 출력
        logger.info("--- 이니시스 인증 응답값 ---");
        logger.info("TID: {}", request.getTid());
        logger.info("PayMethod: {}", request.getPayMethod());
        logger.info("MID: {}", request.getMid());
        logger.info("AuthToken: {}", request.getAuthToken());
        logger.info("AuthUrl: {}", request.getAuthUrl());
        logger.info("NetCancel: {}", request.getNetCancel());
        logger.info("Price: {}", request.getPrice());
        logger.info("Timestamp: {}", request.getTimestamp());
        logger.info("Signature: {}", request.getSignature());
        
        logger.info("--- 주문자 정보 ---");
        logger.info("고객명: {}", request.getCustomerName());
        logger.info("이메일: {}", request.getCustomerEmail());
        logger.info("연락처: {}", request.getCustomerPhone());
        
        logger.info("--- 주문 상품 정보 ---");
        logger.info("상품명: {}", request.getProductName());
        logger.info("수량: {}", request.getQuantity());
        
        // 적립금 사용 처리
        Long finalAmount = request.getPrice().longValue();
        String pointMessage = "";
        
        if (request.getMemberId() != null && request.getUsePoints() != null && request.getUsePoints() > 0) {
            logger.info("--- 적립금 사용 정보 ---");
            logger.info("회원 ID: {}", request.getMemberId());
            logger.info("사용 적립금: {}원", request.getUsePoints());
            
            try {
                Member member = memberRepository.findByMemberId(request.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
                
                if (member.getPoints() < request.getUsePoints()) {
                    throw new IllegalArgumentException("보유 적립금이 부족합니다.");
                }
                
                member.usePoints(request.getUsePoints());
                memberRepository.save(member);
                
                PointHistory pointHistory = new PointHistory(
                    member,
                    PointHistory.PointType.USE,
                    request.getUsePoints(),
                    "결제 시 적립금 사용 (이니시스 TID: " + request.getTid() + ")"
                );
                pointHistoryRepository.save(pointHistory);
                
                finalAmount = request.getPrice().longValue() - request.getUsePoints();
                pointMessage = String.format(" (적립금 %d원 사용, 실결제금액: %d원)", request.getUsePoints(), finalAmount);
                
                logger.info("적립금 사용 완료 - 잔여 적립금: {}원", member.getPoints());
                
            } catch (Exception e) {
                logger.error("적립금 사용 처리 중 오류 발생: {}", e.getMessage());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "ERROR");
                errorResponse.put("message", "적립금 사용 처리 중 오류가 발생했습니다: " + e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        logger.info("=== 이니시스 결제 승인 요청 처리 완료 ===");
        
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "이니시스 결제 승인 요청이 성공적으로 처리되었습니다." + pointMessage);
        response.put("tid", request.getTid());
        response.put("price", request.getPrice());
        response.put("finalAmount", finalAmount);
        response.put("usePoints", request.getUsePoints() != null ? request.getUsePoints() : 0);
        response.put("pgType", "INICIS");
        
        return ResponseEntity.ok(response);
    }
}