package com.example.payment.service;

import com.example.payment.dto.*;
import com.example.payment.entity.Member;
import com.example.payment.entity.Payment;
import com.example.payment.enums.PaymentMethod;
import com.example.payment.enums.PaymentType;
import com.example.payment.factory.PaymentStrategyFactory;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentStrategyFactory paymentStrategyFactory;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    
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
            // Member 엔티티 조회
            Member member = memberRepository.findByMemberId(request.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + request.getMemberId()));

            for (PaymentItem item : paymentItems) {
                
                // 개별 결제 요청 생성
                PaymentConfirmRequest itemRequest = createItemRequest(request, item);
                
                // 전략 선택 및 결제 처리 (에러시 예외 던짐)
                PaymentStrategy strategy = paymentStrategyFactory.getStrategy(item.getPaymentMethod());
                PaymentProcessResult result = strategy.processPayment(itemRequest);
                
                // 결과를 Map으로 변환하여 기존 로직과 호환
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("paymentMethod", result.getPaymentMethod());
                resultMap.put("amount", result.getAmount());
                resultMap.put("tid", result.getTid());
                resultMap.put("pgResult", result.getPgResult());
                
                paymentResults.add(resultMap);
                totalProcessedAmount += item.getAmount();
                
                // PG Provider 결정 (적립금은 null)
                String pgProvider = item.getPaymentMethod() == PaymentMethod.POINTS ? 
                    null : request.getPgProvider();
                
                Payment paymentRecord = new Payment(
                    request.getOrderId(),
                    member,
                    item.getPaymentMethod(),
                    PaymentType.APPROVE,
                    pgProvider,
                    item.getAmount(),
                    request.getProductName(),
                    result.getTid()
                );
                
                // 결제 응답에서 tid 설정
                if (result.getTid() != null) {
                    paymentRecord.setTid(result.getTid());
                    logger.info("결제 TID 저장: {}", result.getTid());
                }
                
                paymentRecords.add(paymentRecord);
                
                logger.info("결제수단: {} 처리 완료", item.getPaymentMethod());
            }
            
            // 모든 개별 결제가 성공한 경우에만 결제 기본 정보 일괄 저장
            paymentRepository.saveAll(paymentRecords);

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
    public Map<String, Object> cancelPayment(Long paymentId) {
        logger.info("=== 결제 취소 처리 시작 ===");
        logger.info("결제 ID: {}", paymentId);
        
        try {
            // Payment ID로 결제 정보 조회
            Payment originalPayment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다: " + paymentId));
            
            // 이미 취소된 결제인지 확인
            if (originalPayment.getPayType() == PaymentType.CANCEL) {
                throw new IllegalStateException("이미 취소된 결제입니다: " + paymentId);
            }
            
            logger.info("결제수단: {}, PG사: {}, 금액: {}원 취소 시작", 
                originalPayment.getPaymentMethod(), 
                originalPayment.getPgProvider(), 
                originalPayment.getPaymentAmount());
            
            // 취소 요청 생성
            PaymentConfirmRequest cancelRequest = createCancelRequest(originalPayment);
            
            // 전략 선택 및 취소 처리 (에러시 예외 던짐)
            PaymentStrategy strategy = paymentStrategyFactory.getStrategy(originalPayment.getPaymentMethod());
            PaymentCancelResult result = strategy.cancelPayment(cancelRequest);
            
            // 취소 내역을 PAYMENTS 테이블에 저장 (PAY_TYPE = CANCEL)
            Payment cancelRecord = new Payment(
                originalPayment.getOrderId(),
                originalPayment.getMember(),
                originalPayment.getPaymentMethod(),
                PaymentType.CANCEL,
                originalPayment.getPgProvider(),
                originalPayment.getPaymentAmount(),
                originalPayment.getProductName(),
                null // 취소는 tid 없음
            );
            
            paymentRepository.save(cancelRecord);
            
            // 전체 결과 구성
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "결제 취소가 성공적으로 처리되었습니다.");
            response.put("paymentId", paymentId);
            response.put("orderId", originalPayment.getOrderId());
            response.put("cancelAmount", originalPayment.getPaymentAmount());
            response.put("paymentMethod", originalPayment.getPaymentMethod());
            response.put("pgProvider", originalPayment.getPgProvider());
            response.put("pgResult", result.getPgResult());
            
            logger.info("=== 결제 취소 처리 완료 ===");
            return response;
            
        } catch (Exception e) {
            logger.error("결제 취소 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("결제 취소 처리 실패: " + e.getMessage(), e);
        }
    }
    
    private PaymentConfirmRequest createCancelRequest(Payment originalPayment) {
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        
        // 기본 정보 설정
        request.setOrderId(originalPayment.getOrderId());
        request.setAmount(originalPayment.getPaymentAmount());
        request.setTotalAmount(originalPayment.getPaymentAmount());
        request.setPaymentMethod(originalPayment.getPaymentMethod());
        request.setPgProvider(originalPayment.getPgProvider());
        request.setProductName(originalPayment.getProductName());
        request.setMemberId(originalPayment.getMember().getMemberId());
        
        // tid 설정 (PG 취소에 필요)
        if (originalPayment.getTid() != null) {
            Map<String, String> authResultMap = new HashMap<>();
            authResultMap.put("tid", originalPayment.getTid());
            request.setAuthResultMap(authResultMap);
        }
        
        // 적립금 사용의 경우 포인트 정보 설정
        if (originalPayment.getPaymentMethod() == PaymentMethod.POINTS) {
            request.setUsePoints(originalPayment.getPaymentAmount());
        }
        
        return request;
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
        long cardAmount = (request.getAmount() != null ? request.getAmount() : request.getTotalAmount())
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
        itemRequest.setPgProvider(originalRequest.getPgProvider());
        
        // 적립금 사용의 경우 포인트 정보 설정
        if (item.getPaymentMethod() == PaymentMethod.POINTS) {
            itemRequest.setUsePoints(item.getAmount());
        }
        
        return itemRequest;
    }
    
    // Entity 리스트를 DTO 리스트로 변환하는 메서드 (병합 로직 포함)
    private List<PaymentDto> convertToDtoList(List<Payment> paymentList) {
        return mergePaymentsByOrderIdAndMethod(paymentList).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 동일 주문번호 + 결제수단의 승인/취소 건을 병합하는 메서드
    private List<Payment> mergePaymentsByOrderIdAndMethod(List<Payment> payments) {
        // 주문번호 + 결제수단을 키로 그룹화
        Map<String, List<Payment>> paymentGroups = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getOrderId() + "_" + p.getPaymentMethod()));
        
        List<Payment> mergedPayments = new ArrayList<>();
        
        // 각 그룹별로 병합 처리
        for (List<Payment> paymentGroup : paymentGroups.values()) {
            // 승인건과 취소건 분류
            List<Payment> approvePayments = paymentGroup.stream()
                    .filter(p -> p.getPayType() == null || p.getPayType().name().equals("APPROVE"))
                    .collect(Collectors.toList());
            List<Payment> cancelPayments = paymentGroup.stream()
                    .filter(p -> p.getPayType() != null && p.getPayType().name().equals("CANCEL"))
                    .collect(Collectors.toList());
            
            if (!approvePayments.isEmpty()) {
                // 승인건을 기준으로 함 (가장 최근 것)
                Payment basePayment = approvePayments.stream()
                        .sorted((a, b) -> b.getPaymentAt().compareTo(a.getPaymentAt()))
                        .findFirst()
                        .get();
                
                // 취소건이 있으면 취소 정보 추가
                if (!cancelPayments.isEmpty()) {
                    Payment cancelPayment = cancelPayments.stream()
                            .sorted((a, b) -> b.getPaymentAt().compareTo(a.getPaymentAt()))
                            .findFirst()
                            .get();
                    
                    // 가상의 병합된 Payment 객체 생성 (취소 정보 포함)
                    Payment mergedPayment = createMergedPayment(basePayment, cancelPayment);
                    mergedPayments.add(mergedPayment);
                } else {
                    // 취소건이 없으면 승인 상태 그대로
                    mergedPayments.add(basePayment);
                }
            } else if (!cancelPayments.isEmpty()) {
                // 승인건 없이 취소건만 있는 경우 (데이터 이상)
                Payment cancelPayment = cancelPayments.get(0);
                mergedPayments.add(cancelPayment);
            }
        }
        
        // 결제일시 기준 내림차순 정렬
        return mergedPayments.stream()
                .sorted((a, b) -> b.getPaymentAt().compareTo(a.getPaymentAt()))
                .collect(Collectors.toList());
    }
    
    // 승인건과 취소건을 병합한 가상의 Payment 객체 생성
    private Payment createMergedPayment(Payment approvePayment, Payment cancelPayment) {
        Payment merged = new Payment();
        
        // 승인건 정보 복사
        merged.setId(approvePayment.getId());
        merged.setOrderId(approvePayment.getOrderId());
        merged.setMember(approvePayment.getMember());
        merged.setPaymentMethod(approvePayment.getPaymentMethod());
        merged.setPayType(approvePayment.getPayType());
        merged.setPgProvider(approvePayment.getPgProvider());
        merged.setPaymentAmount(approvePayment.getPaymentAmount());
        merged.setProductName(approvePayment.getProductName());
        merged.setTid(approvePayment.getTid());
        merged.setPaymentAt(approvePayment.getPaymentAt());
        merged.setCreatedAt(approvePayment.getCreatedAt());
        merged.setUpdatedAt(approvePayment.getUpdatedAt());
        
        // 취소 정보를 별도 필드로 저장 (Payment 엔티티에는 없지만 DTO 변환시 사용)
        merged.setCancelledAt(cancelPayment.getPaymentAt());
        
        return merged;
    }
    
    // Payment를 PaymentDto로 변환 (병합 정보 포함)
    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = PaymentDto.from(payment);
        
        // 취소 정보가 있으면 설정
        if (payment.getCancelledAt() != null) {
            dto.setIsCancelled(true);
            dto.setCancelledAt(payment.getCancelledAt());
            dto.setPayType("CANCEL"); // 취소 완료 상태로 표시
        }
        
        return dto;
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
        
        // Entity를 DTO로 변환
        List<PaymentDto> paymentDtoList = convertToDtoList(paymentList);
        
        // 총 결제금액 계산 (payments 테이블의 모든 데이터는 성공한 결제)
        Long totalAmount = paymentList.stream()
            .mapToLong(Payment::getPaymentAmount)
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
            paymentDtoList  // DTO 리스트 사용
        );
    }
}