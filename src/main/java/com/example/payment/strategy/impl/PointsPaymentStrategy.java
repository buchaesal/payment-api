package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentCancelRequest;
import com.example.payment.dto.PaymentProcessResult;
import com.example.payment.dto.PaymentCancelResult;
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
    public PaymentProcessResult processPayment(PaymentConfirmRequest request) {
        logger.info("=== 적립금 결제 승인 처리 시작 ===");
        
        Long paymentAmount = request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount();
        
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
        
        // 5. 적립금 결과 데이터 구성
        Map<String, Object> pgResult = new HashMap<>();
        pgResult.put("memberId", request.getMemberId());
        pgResult.put("remainingPoints", member.getPoints());
        pgResult.put("usedPoints", paymentAmount);
        
        logger.info("=== 적립금 결제 승인 처리 완료 ===");
        
        return new PaymentProcessResult("POINTS", request.getOrderId(), paymentAmount, null, pgResult);
    }
    
    @Override
    public PaymentCancelResult cancelPayment(PaymentCancelRequest request) {
        logger.info("=== 적립금 결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        logger.info("취소금액: {}원", request.getAmount());
        logger.info("회원 ID: {}", request.getMemberId());

        Long cancelAmount = request.getAmount();

        // 1. 회원 정보 조회
        if (request.getMemberId() == null || request.getMemberId().trim().isEmpty()) {
            throw new IllegalArgumentException("적립금 취소를 위해서는 회원 ID가 필요합니다.");
        }

        Member member = memberRepository.findByMemberId(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + request.getMemberId()));

        // 2. 기존 사용 내역 확인 (검증용)
        PointHistory existingUseHistory = pointHistoryRepository.findByOrderIdAndPointType(
            request.getOrderId(), PointHistory.PointType.USE)
            .orElse(null);

        if (existingUseHistory == null) {
            logger.warn("취소할 적립금 사용 내역을 찾을 수 없습니다: {}", request.getOrderId());
            throw new IllegalArgumentException("취소할 적립금 사용 내역을 찾을 수 없습니다: " + request.getOrderId());
        }

        // 3. 이미 취소된 내역인지 확인
        PointHistory existingRefundHistory = pointHistoryRepository.findByOrderIdAndPointType(
            request.getOrderId(), PointHistory.PointType.REFUND)
            .orElse(null);

        if (existingRefundHistory != null) {
            throw new IllegalStateException("이미 취소된 적립금 사용 내역입니다: " + request.getOrderId());
        }

        // 4. 적립금 환원 처리
        Long originalUsedAmount = existingUseHistory.getPoints();

        // 요청된 취소 금액과 원래 사용 금액이 일치하는지 확인
        if (!originalUsedAmount.equals(cancelAmount)) {
            throw new IllegalArgumentException(
                String.format("취소 요청 금액(%d원)이 원래 사용 금액(%d원)과 일치하지 않습니다.",
                             cancelAmount, originalUsedAmount));
        }

        member.addPoints(cancelAmount);
        memberRepository.save(member);

        logger.info("적립금 환원 완료 - 환원금액: {}원, 현재적립금: {}원", cancelAmount, member.getPoints());

        // 5. 포인트 히스토리 저장 (환불 타입으로)
        PointHistory refundHistory = new PointHistory(
            member,
            PointHistory.PointType.REFUND,
            cancelAmount,
            request.getOrderId()
        );
        pointHistoryRepository.save(refundHistory);

        logger.info("포인트 히스토리 저장 완료 (환불)");

        // 6. 취소 결과 데이터 구성
        Map<String, Object> pgResult = new HashMap<>();
        pgResult.put("memberId", request.getMemberId());
        pgResult.put("refundedPoints", cancelAmount);
        pgResult.put("currentPoints", member.getPoints());
        pgResult.put("canceledAt", java.time.LocalDateTime.now());

        logger.info("=== 적립금 결제 취소 처리 완료 ===");

        return new PaymentCancelResult("POINTS", request.getOrderId(), cancelAmount, pgResult);
    }

    @Override
    public void performNetCancellation(PaymentProcessResult processResult, PaymentConfirmRequest request) {
        // 적립금 결제는 DB 트랜잭션 롤백으로 자동 처리되므로 별도 망취소 불필요
        logger.info("적립금 결제 망취소: DB 트랜잭션 롤백으로 자동 처리됩니다. orderId={}, amount={}",
                   processResult.getOrderId(), processResult.getAmount());
    }
}