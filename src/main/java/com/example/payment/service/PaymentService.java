package com.example.payment.service;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentConfirmResponse;
import com.example.payment.dto.PaymentHistoryResponse;
import com.example.payment.dto.PaymentOrderResponse;
import com.example.payment.dto.PaymentItem;
import com.example.payment.dto.PaymentDto;
import com.example.payment.entity.Member;
import com.example.payment.entity.Payment;
import com.example.payment.enums.PaymentMethod;
import com.example.payment.factory.PaymentStrategyFactory;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Transactional
    public PaymentConfirmResponse processPayment(PaymentConfirmRequest request) {
        
        // 결제 항목 목록 준비
        List<PaymentItem> paymentItems = preparePaymentItems(request);
        
        // 결제 금액 검증
        validatePaymentAmount(request, paymentItems);
        
        // 각 결제수단별 결제 처리
        List<Map<String, Object>> paymentResults = new ArrayList<>();
        List<Payment> paymentRecords = new ArrayList<>();
        Long totalProcessedAmount = 0L;
        
        try {
            for (PaymentItem item : paymentItems) {
                logger.info("결제수단: {}, 금액: {}원 처리 시작", item.getPaymentMethod(), item.getAmount());
                
                // 개별 결제 요청 생성
                PaymentConfirmRequest itemRequest = createItemRequest(request, item);
                
                // 전략 선택 및 결제 처리
                PaymentStrategy strategy = paymentStrategyFactory.getStrategy(item.getPaymentMethod());
                Map<String, Object> result = strategy.processPayment(itemRequest);
                
                paymentResults.add(result);
                totalProcessedAmount += item.getAmount();
                
                // 결제 성공시에만 결제 기본 정보 준비
                String paymentStatus = "SUCCESS".equals(result.get("status")) ? "SUCCESS" : "FAILED";
                
                // Member 엔티티 조회
                Member member = memberRepository.findByMemberId(request.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + request.getMemberId()));
                
                Payment paymentRecord = new Payment(
                    request.getOrderId(),
                    member,
                    item.getPaymentMethod(),
                    item.getAmount(),
                    request.getProductName(),
                    paymentStatus
                );
                paymentRecords.add(paymentRecord);
                
                logger.info("결제수단: {} 처리 완료", item.getPaymentMethod());
                
                // 개별 결제가 실패한 경우 전체 롤백
                if (!"SUCCESS".equals(paymentStatus)) {
                    throw new RuntimeException("결제 실패: " + result.get("message"));
                }
            }
            
            // 모든 개별 결제가 성공한 경우에만 결제 기본 정보 일괄 저장
            logger.info("결제 기본 정보 일괄 저장 시작 - {} 건", paymentRecords.size());
            paymentRepository.saveAll(paymentRecords);
            logger.info("결제 기본 정보 저장 완료");
            
            logger.info("=== 복합결제 처리 완료 ===");
            return new PaymentConfirmResponse(
                "SUCCESS",
                "복합결제가 성공적으로 처리되었습니다.",
                request.getOrderId(),
                request.getTotalAmount(),
                totalProcessedAmount,
                paymentResults,
                paymentItems.size()
            );
            
        } catch (Exception e) {
            logger.error("복합결제 처리 중 오류 발생: {}", e.getMessage());
            // 트랜잭션이 롤백되므로 이미 처리된 결제들도 자동으로 롤백됩니다
            throw new RuntimeException("복합결제 처리 실패: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public Map<String, Object> cancelPayment(PaymentConfirmRequest request) {
        logger.info("=== 복합결제 취소 처리 시작 ===");
        logger.info("주문번호: {}", request.getOrderId());
        
        // 결제 항목 목록 준비
        List<PaymentItem> paymentItems = preparePaymentItems(request);
        
        // 각 결제수단별 취소 처리
        List<Map<String, Object>> cancelResults = new ArrayList<>();
        
        for (PaymentItem item : paymentItems) {
            logger.info("결제수단: {}, 금액: {}원 취소 시작", item.getPaymentMethod(), item.getAmount());
            
            // 개별 취소 요청 생성
            PaymentConfirmRequest itemRequest = createItemRequest(request, item);
            
            // 전략 선택 및 취소 처리
            PaymentStrategy strategy = paymentStrategyFactory.getStrategy(item.getPaymentMethod());
            Map<String, Object> result = strategy.cancelPayment(itemRequest);
            
            cancelResults.add(result);
            
            logger.info("결제수단: {} 취소 완료", item.getPaymentMethod());
        }
        
        // 전체 결과 구성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "복합결제 취소가 성공적으로 처리되었습니다.");
        response.put("orderId", request.getOrderId());
        response.put("cancelResults", cancelResults);
        
        logger.info("=== 복합결제 취소 처리 완료 ===");
        return response;
    }
    
    private List<PaymentItem> preparePaymentItems(PaymentConfirmRequest request) {
        // 신규 복합결제 방식 우선
        if (request.getPaymentItems() != null && !request.getPaymentItems().isEmpty()) {
            return request.getPaymentItems();
        }
        
        // 하위호환: 기존 단일결제 방식 지원
        List<PaymentItem> items = new ArrayList<>();
        
        // 적립금 사용이 있는 경우
        if (request.getUsePoints() != null && request.getUsePoints() > 0) {
            items.add(new PaymentItem(PaymentMethod.POINTS, request.getUsePoints()));
        }
        
        // 카드 결제 (전체금액 - 적립금)
        Long cardAmount = (request.getAmount() != null ? request.getAmount() : request.getTotalAmount())
                         - (request.getUsePoints() != null ? request.getUsePoints() : 0L);
        
        if (cardAmount > 0) {
            items.add(new PaymentItem(PaymentMethod.CARD, cardAmount));
        }
        
        return items;
    }
    
    private void validatePaymentAmount(PaymentConfirmRequest request, List<PaymentItem> paymentItems) {
        Long totalItemAmount = paymentItems.stream()
                .mapToLong(PaymentItem::getAmount)
                .sum();
        
        Long expectedAmount = request.getTotalAmount() != null ? 
                             request.getTotalAmount() : request.getAmount();
        
        // expectedAmount가 null인 경우 처리
        if (expectedAmount == null) {
            throw new IllegalArgumentException("총 결제금액(totalAmount 또는 amount)이 설정되지 않았습니다.");
        }
        
        if (!totalItemAmount.equals(expectedAmount)) {
            throw new IllegalArgumentException(
                String.format("결제금액이 일치하지 않습니다. 요청금액: %d원, 실제금액: %d원", 
                             expectedAmount, totalItemAmount));
        }
    }
    
    private PaymentConfirmRequest createItemRequest(PaymentConfirmRequest originalRequest, PaymentItem item) {
        PaymentConfirmRequest itemRequest = new PaymentConfirmRequest();
        
        // 기본 정보 복사
        itemRequest.setOrderId(originalRequest.getOrderId());
        itemRequest.setTotalAmount(item.getAmount());
        itemRequest.setAmount(item.getAmount()); // 하위호환
        itemRequest.setPaymentMethod(item.getPaymentMethod()); // 하위호환
        itemRequest.setCustomerName(originalRequest.getCustomerName());
        itemRequest.setCustomerEmail(originalRequest.getCustomerEmail());
        itemRequest.setCustomerPhone(originalRequest.getCustomerPhone());
        itemRequest.setProductName(originalRequest.getProductName());
        itemRequest.setQuantity(originalRequest.getQuantity());
        itemRequest.setMemberId(originalRequest.getMemberId());
        itemRequest.setAuthResultMap(originalRequest.getAuthResultMap());
        
        // 적립금 사용의 경우 포인트 정보 설정
        if (item.getPaymentMethod() == PaymentMethod.POINTS) {
            itemRequest.setUsePoints(item.getAmount());
        }
        
        return itemRequest;
    }
    
    // Entity를 DTO로 변환하는 메서드
    private PaymentDto convertToDto(Payment payment) {
        return new PaymentDto(
            payment.getId(),
            payment.getOrderId(),
            payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
            payment.getPaymentAmount(),
            payment.getPaymentStatus(),
            payment.getPaymentAt(),
            payment.getProductName(),
            payment.getMember() != null ? payment.getMember().getMemberId() : null
        );
    }
    
    // Entity 리스트를 DTO 리스트로 변환하는 메서드
    private List<PaymentDto> convertToDtoList(List<Payment> paymentList) {
        return paymentList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PaymentHistoryResponse getPaymentHistory(String memberId) {
        logger.info("=== 결제내역 조회 시작 ===");
        logger.info("회원ID: {}", memberId);
        
        // 회원 존재 여부 확인
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + memberId));
        
        // 해당 회원의 모든 결제 내역 조회
        List<Payment> paymentList = paymentRepository.findByMemberOrderByPaymentAtDesc(member);
        
        // Entity를 DTO로 변환
        List<PaymentDto> paymentDtoList = convertToDtoList(paymentList);
        
        logger.info("조회된 결제내역 수: {}건", paymentList.size());
        
        logger.info("=== 결제내역 조회 완료 ===");
        return new PaymentHistoryResponse(
            "SUCCESS",
            "결제내역 조회가 완료되었습니다.",
            memberId,
            paymentDtoList.size(),
            paymentDtoList
        );
    }
    
    public PaymentOrderResponse getPaymentByOrderId(String orderId) {
        logger.info("=== 주문번호로 결제정보 조회 시작 ===");
        logger.info("주문번호: {}", orderId);
        
        // 해당 주문번호의 모든 결제 내역 조회
        List<Payment> paymentList = paymentRepository.findByOrderId(orderId);
        
        if (paymentList.isEmpty()) {
            throw new IllegalArgumentException("해당 주문번호로 결제 내역을 찾을 수 없습니다: " + orderId);
        }
        
        logger.info("조회된 결제내역 수: {}건", paymentList.size());
        
        // 총 결제금액 계산
        Long totalAmount = paymentList.stream()
            .filter(payment -> "SUCCESS".equals(payment.getPaymentStatus()))
            .mapToLong(payment -> payment.getPaymentAmount())
            .sum();
        
        // 상품명은 첫 번째 결제 기록에서 가져옴 (모든 결제가 같은 상품이므로)
        String productName = paymentList.get(0).getProductName();
        
        logger.info("=== 주문번호로 결제정보 조회 완료 ===");
        return new PaymentOrderResponse(
            "SUCCESS",
            "결제정보 조회가 완료되었습니다.",
            orderId,
            productName,
            totalAmount,
            paymentList.size(),
            paymentList
        );
    }
}