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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentStrategyFactory paymentStrategyFactory;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentService paymentService;

    private Member testMember;
    private PaymentConfirmRequest paymentRequest;
    private PaymentProcessResult mockProcessResult;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 데이터
        testMember = new Member("test123", "홍길동", "test@test.com", "01012345678");
        testMember.setId(1L);
        testMember.addPoints(5000L);

        // 테스트용 결제 요청 데이터
        paymentRequest = new PaymentConfirmRequest();
        paymentRequest.setOrderId("ORDER_" + System.currentTimeMillis());
        paymentRequest.setMemberId("test123");
        paymentRequest.setTotalAmount(10000L);
        paymentRequest.setCustomerName("홍길동");
        paymentRequest.setCustomerEmail("test@test.com");
        paymentRequest.setProductName("테스트 상품");
        paymentRequest.setPgProvider("toss");

        // 테스트용 결제 처리 결과
        mockProcessResult = new PaymentProcessResult();
        mockProcessResult.setPaymentMethod("CARD");
        mockProcessResult.setAmount(10000L);
        mockProcessResult.setTid("TOSS_TID_12345");
        mockProcessResult.setPgResult(Map.of("status", "success"));
    }

    @Test
    @DisplayName("단일 카드 결제 성공 테스트")
    void processPayment_SingleCardPayment_Success() {
        // Given
        List<PaymentItem> paymentItems = List.of(
            new PaymentItem(PaymentMethod.CARD, 10000L)
        );
        paymentRequest.setPaymentItems(paymentItems);

        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(testMember));
        when(paymentStrategyFactory.getStrategy(PaymentMethod.CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any())).thenReturn(mockProcessResult);
        when(paymentRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // When
        PaymentConfirmResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("복합결제가 성공적으로 처리되었습니다.", response.getMessage());
        assertEquals(paymentRequest.getOrderId(), response.getOrderId());
        assertEquals(10000L, response.getTotalAmount());
        assertEquals(10000L, response.getProcessedAmount());
        assertEquals(1, response.getPaymentCount());

        verify(paymentStrategyFactory).getStrategy(PaymentMethod.CARD);
        verify(paymentStrategy).processPayment(any());
        verify(paymentRepository).saveAll(any());
    }

    @Test
    @DisplayName("복합 결제 (카드 + 적립금) 성공 테스트")
    void processPayment_CombinedPayment_Success() {
        // Given
        List<PaymentItem> paymentItems = List.of(
            new PaymentItem(PaymentMethod.CARD, 7000L),
            new PaymentItem(PaymentMethod.POINTS, 3000L)
        );
        paymentRequest.setPaymentItems(paymentItems);

        PaymentProcessResult cardResult = new PaymentProcessResult();
        cardResult.setPaymentMethod("CARD");
        cardResult.setAmount(7000L);
        cardResult.setTid("CARD_TID_12345");

        PaymentProcessResult pointsResult = new PaymentProcessResult();
        pointsResult.setPaymentMethod("POINTS");
        pointsResult.setAmount(3000L);
        pointsResult.setTid(null);

        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(testMember));
        when(paymentStrategyFactory.getStrategy(PaymentMethod.CARD)).thenReturn(paymentStrategy);
        when(paymentStrategyFactory.getStrategy(PaymentMethod.POINTS)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any()))
            .thenReturn(cardResult)
            .thenReturn(pointsResult);
        when(paymentRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // When
        PaymentConfirmResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(10000L, response.getProcessedAmount());
        assertEquals(2, response.getPaymentCount());

        verify(paymentStrategyFactory).getStrategy(PaymentMethod.CARD);
        verify(paymentStrategyFactory).getStrategy(PaymentMethod.POINTS);
        verify(paymentStrategy, times(2)).processPayment(any());
    }

    @Test
    @DisplayName("존재하지 않는 회원 결제 시 예외 발생")
    void processPayment_MemberNotFound_ThrowsException() {
        // Given
        List<PaymentItem> paymentItems = List.of(
            new PaymentItem(PaymentMethod.CARD, 10000L)
        );
        paymentRequest.setPaymentItems(paymentItems);

        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processPayment(paymentRequest)
        );

        assertEquals("존재하지 않는 회원입니다: test123", exception.getMessage());
        verify(paymentStrategyFactory, never()).getStrategy(any());
        verify(paymentStrategy, never()).processPayment(any());
    }

    @Test
    @DisplayName("결제 금액 불일치 시 예외 발생")
    void processPayment_AmountMismatch_ThrowsException() {
        // Given
        List<PaymentItem> paymentItems = List.of(
            new PaymentItem(PaymentMethod.CARD, 5000L) // 요청 금액 10000원과 불일치
        );
        paymentRequest.setPaymentItems(paymentItems);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processPayment(paymentRequest)
        );

        assertTrue(exception.getMessage().contains("결제금액이 일치하지 않습니다"));
    }

    @Test
    @DisplayName("결제 중 오류 발생 시 망취소 처리")
    void processPayment_ErrorOccurs_PerformsNetCancellation() {
        // Given
        List<PaymentItem> paymentItems = List.of(
            new PaymentItem(PaymentMethod.CARD, 7000L),
            new PaymentItem(PaymentMethod.POINTS, 3000L)
        );
        paymentRequest.setPaymentItems(paymentItems);

        PaymentProcessResult cardResult = new PaymentProcessResult();
        cardResult.setPaymentMethod("CARD");
        cardResult.setAmount(7000L);
        cardResult.setTid("CARD_TID_12345");

        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(testMember));
        when(paymentStrategyFactory.getStrategy(PaymentMethod.CARD)).thenReturn(paymentStrategy);
        when(paymentStrategyFactory.getStrategy(PaymentMethod.POINTS)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any()))
            .thenReturn(cardResult) // 첫 번째 카드 결제 성공
            .thenThrow(new RuntimeException("적립금 결제 실패")); // 두 번째 적립금 결제 실패

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> paymentService.processPayment(paymentRequest)
        );

        assertTrue(exception.getMessage().contains("복합결제 처리 실패"));
        verify(paymentStrategy).performNetCancellation(eq(cardResult), any()); // 망취소 호출 확인
    }

    @Test
    @DisplayName("결제 취소 성공 테스트")
    void cancelPayment_Success() {
        // Given
        Long paymentId = 1L;
        Payment originalPayment = new Payment("ORDER_12345", testMember, PaymentMethod.CARD, PaymentType.APPROVE,
                "toss", 10000L, "테스트 상품", "TOSS_TID_12345");
        originalPayment.setId(paymentId);

        PaymentCancelResult cancelResult = new PaymentCancelResult();
        cancelResult.setPgResult(Map.of("cancelStatus", "success"));

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(originalPayment));
        when(paymentStrategyFactory.getStrategy(PaymentMethod.CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.cancelPayment(any())).thenReturn(cancelResult);
        when(paymentRepository.save(any())).thenReturn(new Payment());

        // When
        Map<String, Object> response = paymentService.cancelPayment(paymentId);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.get("status"));
        assertEquals("결제 취소가 성공적으로 처리되었습니다.", response.get("message"));
        assertEquals(paymentId, response.get("paymentId"));
        assertEquals("ORDER_12345", response.get("orderId"));
        assertEquals(10000L, response.get("cancelAmount"));
        assertEquals(PaymentMethod.CARD, response.get("paymentMethod"));

        verify(paymentStrategy).cancelPayment(any());
        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 결제 취소 시 예외 발생")
    void cancelPayment_PaymentNotFound_ThrowsException() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> paymentService.cancelPayment(paymentId)
        );

        assertTrue(exception.getMessage().contains("결제 취소 처리 실패"));
        verify(paymentStrategy, never()).cancelPayment(any());
    }

    @Test
    @DisplayName("이미 취소된 결제 재취소 시 예외 발생")
    void cancelPayment_AlreadyCancelled_ThrowsException() {
        // Given
        Long paymentId = 1L;
        Payment cancelledPayment = new Payment("ORDER_12345", testMember, PaymentMethod.CARD, PaymentType.CANCEL,
                "toss", 10000L, "테스트 상품", null);
        cancelledPayment.setId(paymentId);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(cancelledPayment));

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> paymentService.cancelPayment(paymentId)
        );

        assertTrue(exception.getMessage().contains("결제 취소 처리 실패"));
        verify(paymentStrategy, never()).cancelPayment(any());
    }

    @Test
    @DisplayName("회원별 결제 내역 조회 성공 테스트")
    void getPaymentHistory_Success() {
        // Given
        String memberId = "test123";
        List<Payment> paymentList = createTestPaymentList();

        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(testMember));
        when(paymentRepository.findByMemberOrderByPaymentAtDesc(testMember)).thenReturn(paymentList);

        // When
        PaymentHistoryResponse response = paymentService.getPaymentHistory(memberId);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("결제내역 조회가 완료되었습니다.", response.getMessage());
        assertEquals(memberId, response.getMemberId());
        assertTrue(response.getPaymentCount() >= 0);
        assertNotNull(response.getPayments());

        verify(memberRepository).findByMemberId(memberId);
        verify(paymentRepository).findByMemberOrderByPaymentAtDesc(testMember);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 결제 내역 조회 시 예외 발생")
    void getPaymentHistory_MemberNotFound_ThrowsException() {
        // Given
        String memberId = "nonexistent";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.getPaymentHistory(memberId)
        );

        assertEquals("존재하지 않는 회원입니다: nonexistent", exception.getMessage());
        verify(paymentRepository, never()).findByMemberOrderByPaymentAtDesc(any());
    }

    @Test
    @DisplayName("주문번호로 결제 정보 조회 성공 테스트")
    void getPaymentByOrderId_Success() {
        // Given
        String orderId = "ORDER_12345";
        List<Payment> paymentList = createTestPaymentList();
        paymentList.forEach(payment -> payment.setOrderId(orderId));

        when(paymentRepository.findByOrderId(orderId)).thenReturn(paymentList);

        // When
        PaymentOrderResponse response = paymentService.getPaymentByOrderId(orderId);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("결제정보 조회가 완료되었습니다.", response.getMessage());
        assertEquals(orderId, response.getOrderId());
        assertTrue(response.getTotalAmount() > 0);
        assertTrue(response.getPaymentCount() > 0);
        assertNotNull(response.getPayments());

        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("존재하지 않는 주문번호 조회 시 예외 발생")
    void getPaymentByOrderId_OrderNotFound_ThrowsException() {
        // Given
        String orderId = "NONEXISTENT_ORDER";
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.getPaymentByOrderId(orderId)
        );

        assertTrue(exception.getMessage().contains("해당 주문번호로 결제 내역을 찾을 수 없습니다"));
    }

    /**
     * 테스트용 결제 데이터 리스트 생성
     */
    private List<Payment> createTestPaymentList() {
        Payment payment1 = new Payment("ORDER_001", testMember, PaymentMethod.CARD, PaymentType.APPROVE,
                "toss", 10000L, "테스트 상품 1", "TID_001");
        payment1.setId(1L);
        payment1.setPaymentAt(LocalDateTime.now());

        Payment payment2 = new Payment("ORDER_002", testMember, PaymentMethod.POINTS, PaymentType.APPROVE,
                null, 5000L, "테스트 상품 2", null);
        payment2.setId(2L);
        payment2.setPaymentAt(LocalDateTime.now().minusHours(1));

        return List.of(payment1, payment2);
    }
}