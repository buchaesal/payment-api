package com.example.payment.integration;

import com.example.payment.dto.*;
import com.example.payment.entity.Member;
import com.example.payment.entity.Payment;
import com.example.payment.enums.PaymentMethod;
import com.example.payment.enums.PaymentType;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.MemberService;
import com.example.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("결제 시스템 통합 테스트")
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원 가입부터 결제까지 전체 플로우 테스트")
    void fullPaymentFlow_Success() throws Exception {
        // 1. 회원 가입
        MemberSignupRequest signupRequest = new MemberSignupRequest();
        signupRequest.setMemberId("integrationTest");
        signupRequest.setName("통합테스트");
        signupRequest.setEmail("integration@test.com");
        signupRequest.setPhone("01012345678");

        String signupJson = objectMapper.writeValueAsString(signupRequest);

        mockMvc.perform(post("/api/member/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.member.memberId").value("integrationTest"))
                .andExpect(jsonPath("$.member.points").value(1000))
                .andDo(print());

        // 2. 회원 로그인 확인
        mockMvc.perform(post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"integrationTest\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("integrationTest"))
                .andExpect(jsonPath("$.points").value(1000))
                .andDo(print());

        // 3. 결제 처리 (카드 + 적립금 복합결제)
        PaymentConfirmRequest paymentRequest = new PaymentConfirmRequest();
        paymentRequest.setOrderId("INTEGRATION_ORDER_" + System.currentTimeMillis());
        paymentRequest.setMemberId("integrationTest");
        paymentRequest.setTotalAmount(15000L);
        paymentRequest.setCustomerName("통합테스트");
        paymentRequest.setCustomerEmail("integration@test.com");
        paymentRequest.setProductName("통합테스트 상품");
        paymentRequest.setPgProvider("toss");

        // 복합결제 설정: 카드 14000원 + 적립금 1000원
        List<PaymentItem> paymentItems = List.of(
            new PaymentItem(PaymentMethod.CARD, 14000L),
            new PaymentItem(PaymentMethod.POINTS, 1000L)
        );
        paymentRequest.setPaymentItems(paymentItems);

        String paymentJson = objectMapper.writeValueAsString(paymentRequest);

        mockMvc.perform(post("/api/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalAmount").value(15000))
                .andExpect(jsonPath("$.processedAmount").value(15000))
                .andExpect(jsonPath("$.paymentCount").value(2))
                .andDo(print());

        // 4. 결제 후 회원 포인트 확인 (1000 - 1000 = 0)
        Optional<Member> updatedMember = memberRepository.findByMemberId("integrationTest");
        assertTrue(updatedMember.isPresent());
        assertEquals(0L, updatedMember.get().getPoints());

        // 5. 결제 내역 조회
        mockMvc.perform(get("/api/payment/history/integrationTest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.memberId").value("integrationTest"))
                .andExpect(jsonPath("$.paymentCount").value(2)) // 카드 + 적립금
                .andDo(print());

        // 6. 주문번호로 결제 정보 조회
        mockMvc.perform(get("/api/payment/order/" + paymentRequest.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.orderId").value(paymentRequest.getOrderId()))
                .andExpect(jsonPath("$.totalAmount").value(15000))
                .andExpect(jsonPath("$.paymentCount").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("결제 취소 전체 플로우 테스트")
    void paymentCancelFlow_Success() throws Exception {
        // 1. 테스트 데이터에서 기존 결제 조회
        List<Payment> existingPayments = paymentRepository.findByOrderId("ORDER_003");
        assertFalse(existingPayments.isEmpty());

        Payment targetPayment = existingPayments.stream()
            .filter(p -> p.getPayType() == PaymentType.APPROVE)
            .findFirst()
            .orElseThrow();

        Long paymentId = targetPayment.getId();

        // 2. 결제 취소 요청
        mockMvc.perform(post("/api/payment/cancel/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.orderId").value("ORDER_003"))
                .andExpect(jsonPath("$.cancelAmount").value(50000))
                .andDo(print());

        // 3. 취소 후 결제 내역 확인 - 취소 레코드가 추가되었는지 확인
        List<Payment> cancelledPayments = paymentRepository.findByOrderId("ORDER_003");
        long cancelCount = cancelledPayments.stream()
            .filter(p -> p.getPayType() == PaymentType.CANCEL)
            .count();

        assertEquals(1, cancelCount, "취소 레코드가 생성되어야 함");
    }

    @Test
    @DisplayName("적립금 내역 조회 통합 테스트")
    void pointHistoryFlow_Success() throws Exception {
        // 테스트 데이터의 testuser1 회원의 적립금 내역 조회
        mockMvc.perform(get("/api/member/testuser1/points/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.pointHistories").isArray())
                .andExpect(jsonPath("$.pointHistories.length()").value(3)) // EARN, USE, REFUND
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 에러 응답")
    void nonExistentMember_ReturnsError() throws Exception {
        // 존재하지 않는 회원 로그인 시도
        mockMvc.perform(post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"nonexistent\"}"))
                .andExpect(status().is5xxServerError()) // RuntimeException으로 처리됨
                .andDo(print());

        // 존재하지 않는 회원의 결제 내역 조회
        mockMvc.perform(get("/api/payment/history/nonexistent"))
                .andExpect(status().is5xxServerError()) // RuntimeException으로 처리됨
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 에러 응답")
    void nonExistentOrder_ReturnsError() throws Exception {
        mockMvc.perform(get("/api/payment/order/NONEXISTENT_ORDER"))
                .andExpect(status().is5xxServerError()) // RuntimeException으로 처리됨
                .andDo(print());
    }

    @Test
    @DisplayName("API 헬스체크 테스트")
    void healthCheck_Success() throws Exception {
        // Payment API 헬스체크
        mockMvc.perform(get("/api/payment/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());

        // Inicis API 헬스체크
        mockMvc.perform(get("/api/inicis/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("데이터베이스 연동 확인 테스트")
    void databaseConnection_Success() {
        // Repository를 통한 데이터 조회 확인
        List<Member> members = memberRepository.findAll();
        assertFalse(members.isEmpty(), "테스트 데이터가 로드되어야 함");

        List<Payment> payments = paymentRepository.findAll();
        assertFalse(payments.isEmpty(), "테스트 결제 데이터가 로드되어야 함");

        // 특정 회원 조회
        Optional<Member> testUser1 = memberRepository.findByMemberId("testuser1");
        assertTrue(testUser1.isPresent(), "testuser1이 존재해야 함");
        assertEquals("홍길동", testUser1.get().getName());
        assertEquals(5000L, testUser1.get().getPoints());
    }
}