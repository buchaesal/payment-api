package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.entity.Member;
import com.example.payment.entity.PointHistory;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PointHistoryRepository;
import com.example.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PointsPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(PointsPaymentStrategy.class);
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    
    @Override
    public Map<String, Object> processPayment(PaymentConfirmRequest request) {
        logger.info("=== 적립금 결제 승인 처리 시작 ===");
        
        Long paymentAmount = request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount();
        
        try {
            // 1. 회원 정보 조회
            if (request.getMemberId() == null || request.getMemberId().trim().isEmpty()) {
                throw new IllegalArgumentException("적립금 결제를 위해서는 회원 ID가 필요합니다.");
            }
            
            Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + request.getMemberId()));
            
            // 2. 적립금 잔액 확인
            if (member.getPoints() < paymentAmount) {
                logger.warn("적립금 잔액 부족 - 보유: {}원, 요청: {}원", member.getPoints(), paymentAmount);
                throw new IllegalArgumentException(
                    String.format("적립금 잔액이 부족합니다. 보유: %d원, 결제요청: %d원", 
                                 member.getPoints(), paymentAmount));
            }
            
            // 3. 적립금 차감 처리
            member.usePoints(paymentAmount);
            memberRepository.save(member);
            
            logger.info("적립금 차감 완료 - 차감금액: {}원, 잔여적립금: {}원", paymentAmount, member.getPoints());
            
            // 4. 포인트 히스토리 저장
            PointHistory pointHistory = new PointHistory(
                member,
                PointHistory.PointType.USE,
                paymentAmount,
                request.getOrderId()
            );
            pointHistoryRepository.save(pointHistory);
            
            logger.info("포인트 히스토리 저장 완료");
            
            // 5. 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "적립금 결제가 성공적으로 처리되었습니다.");
            response.put("paymentMethod", "POINTS");
            response.put("orderId", request.getOrderId());
            response.put("amount", paymentAmount);
            response.put("memberId", request.getMemberId());
            response.put("remainingPoints", member.getPoints());
            
            logger.info("=== 적립금 결제 승인 처리 완료 ===");
            return response;
            
        } catch (IllegalArgumentException e) {
            logger.error("적립금 결제 검증 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("paymentMethod", "POINTS");
            errorResponse.put("orderId", request.getOrderId());
            return errorResponse;
            
        } catch (Exception e) {
            logger.error("적립금 결제 처리 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", "적립금 결제 처리 중 오류가 발생했습니다.");
            errorResponse.put("paymentMethod", "POINTS");
            errorResponse.put("orderId", request.getOrderId());
            return errorResponse;
        }
    }
    
    @Override
    public Map<String, Object> cancelPayment(PaymentConfirmRequest request) {
        logger.info("=== 적립금 결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("취소금액: {}원", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        logger.info("회원 ID: {}", request.getMemberId());
        
        // TODO: 실제 적립금 결제 취소 로직 구현
        // - 적립금 환원 처리
        // - 포인트 히스토리 저장
        // - 결제 취소 결과 저장
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "적립금 결제가 성공적으로 취소되었습니다.");
        response.put("paymentMethod", "POINTS");
        response.put("orderId", request.getOrderId());
        response.put("cancelAmount", request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount());
        response.put("memberId", request.getMemberId());
        
        logger.info("=== 적립금 결제 취소 처리 완료 ===");
        return response;
    }
}