package com.example.payment.service;

import com.example.payment.dto.MemberResponse;
import com.example.payment.dto.MemberSignupRequest;
import com.example.payment.dto.PointHistoryResponse;
import com.example.payment.entity.Member;
import com.example.payment.entity.PointHistory;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberSignupRequest signupRequest;
    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 가입 요청 데이터
        signupRequest = new MemberSignupRequest();
        signupRequest.setMemberId("test123");
        signupRequest.setName("홍길동");
        signupRequest.setEmail("test@test.com");
        signupRequest.setPhone("01012345678");

        // 테스트용 회원 데이터
        testMember = new Member("test123", "홍길동", "test@test.com", "01012345678");
        testMember.setId(1L);
        testMember.addPoints(1000L);
    }

    @Test
    @DisplayName("회원 가입 성공 테스트")
    void signup_Success() {
        // Given
        when(memberRepository.existsByMemberId("test123")).thenReturn(false);
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(pointHistoryRepository.save(any(PointHistory.class))).thenReturn(new PointHistory());

        // When
        MemberResponse response = memberService.signup(signupRequest);

        // Then
        assertNotNull(response);
        assertEquals("test123", response.getMemberId());
        assertEquals("홍길동", response.getName());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("01012345678", response.getPhone());
        assertEquals(1000L, response.getPoints());

        // Member 저장 검증
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertEquals("test123", savedMember.getMemberId());
        assertEquals("홍길동", savedMember.getName());
        assertEquals(1000L, savedMember.getPoints());

        // PointHistory 저장 검증
        ArgumentCaptor<PointHistory> pointCaptor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(pointCaptor.capture());
        PointHistory savedPointHistory = pointCaptor.getValue();
        assertEquals(PointHistory.PointType.EARN, savedPointHistory.getPointTypeEnum());
        assertEquals(1000L, savedPointHistory.getPointAmount());
        assertNull(savedPointHistory.getOrderId()); // 회원가입 적립금은 주문번호 없음
    }

    @Test
    @DisplayName("중복된 회원 ID로 가입 시 예외 발생")
    void signup_DuplicateMemberId_ThrowsException() {
        // Given
        when(memberRepository.existsByMemberId("test123")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.signup(signupRequest)
        );

        assertEquals("이미 존재하는 회원 ID입니다.", exception.getMessage());
        verify(memberRepository, never()).save(any());
        verify(pointHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("중복된 이메일로 가입 시 예외 발생")
    void signup_DuplicateEmail_ThrowsException() {
        // Given
        when(memberRepository.existsByMemberId("test123")).thenReturn(false);
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.signup(signupRequest)
        );

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        verify(memberRepository, never()).save(any());
        verify(pointHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일이 null인 경우 중복 검사 건너뛰기")
    void signup_NullEmail_SkipsDuplicateCheck() {
        // Given
        signupRequest.setEmail(null);
        when(memberRepository.existsByMemberId("test123")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(pointHistoryRepository.save(any(PointHistory.class))).thenReturn(new PointHistory());

        // When
        MemberResponse response = memberService.signup(signupRequest);

        // Then
        assertNotNull(response);
        assertEquals("test123", response.getMemberId());
        verify(memberRepository, never()).existsByEmail(anyString());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() {
        // Given
        String memberId = "test123";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(testMember));

        // When
        MemberResponse response = memberService.login(memberId);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test123", response.getMemberId());
        assertEquals("홍길동", response.getName());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("01012345678", response.getPhone());
        assertEquals(1000L, response.getPoints());

        verify(memberRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 로그인 시 예외 발생")
    void login_MemberNotFound_ThrowsException() {
        // Given
        String memberId = "nonexistent";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.login(memberId)
        );

        assertEquals("존재하지 않는 회원입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("회원 ID로 회원 조회 성공 테스트")
    void getMemberById_Success() {
        // Given
        String memberId = "test123";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(testMember));

        // When
        MemberResponse response = memberService.getMemberById(memberId);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test123", response.getMemberId());
        assertEquals("홍길동", response.getName());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("01012345678", response.getPhone());
        assertEquals(1000L, response.getPoints());

        verify(memberRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외 발생")
    void getMemberById_MemberNotFound_ThrowsException() {
        // Given
        String memberId = "nonexistent";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.getMemberById(memberId)
        );

        assertEquals("존재하지 않는 회원입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("적립금 내역 조회 성공 테스트")
    void getPointHistory_Success() {
        // Given
        String memberId = "test123";
        List<PointHistory> pointHistories = createTestPointHistories();

        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(testMember));
        when(pointHistoryRepository.findByMemberOrderByCreatedAtDesc(testMember)).thenReturn(pointHistories);

        // When
        List<PointHistoryResponse> responses = memberService.getPointHistory(memberId);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        // 첫 번째 적립금 내역 확인 (최신순)
        PointHistoryResponse firstResponse = responses.get(0);
        assertEquals(PointHistory.PointType.EARN, firstResponse.getPointType());
        assertEquals(500L, firstResponse.getPointAmount());
        assertEquals("ORDER_003", firstResponse.getOrderId());

        // 두 번째 적립금 내역 확인
        PointHistoryResponse secondResponse = responses.get(1);
        assertEquals(PointHistory.PointType.USE, secondResponse.getPointType());
        assertEquals(300L, secondResponse.getPointAmount());
        assertEquals("ORDER_002", secondResponse.getOrderId());

        // 세 번째 적립금 내역 확인
        PointHistoryResponse thirdResponse = responses.get(2);
        assertEquals(PointHistory.PointType.EARN, thirdResponse.getPointType());
        assertEquals(1000L, thirdResponse.getPointAmount());
        assertNull(thirdResponse.getOrderId()); // 회원가입 적립금

        verify(memberRepository).findByMemberId(memberId);
        verify(pointHistoryRepository).findByMemberOrderByCreatedAtDesc(testMember);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 적립금 내역 조회 시 예외 발생")
    void getPointHistory_MemberNotFound_ThrowsException() {
        // Given
        String memberId = "nonexistent";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.getPointHistory(memberId)
        );

        assertEquals("존재하지 않는 회원입니다.", exception.getMessage());
        verify(pointHistoryRepository, never()).findByMemberOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("적립금 내역이 없는 회원 조회 시 빈 리스트 반환")
    void getPointHistory_EmptyHistory_ReturnsEmptyList() {
        // Given
        String memberId = "test123";
        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(testMember));
        when(pointHistoryRepository.findByMemberOrderByCreatedAtDesc(testMember)).thenReturn(List.of());

        // When
        List<PointHistoryResponse> responses = memberService.getPointHistory(memberId);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(memberRepository).findByMemberId(memberId);
        verify(pointHistoryRepository).findByMemberOrderByCreatedAtDesc(testMember);
    }

    @Test
    @DisplayName("회원가입 시 적립금 자동 지급 확인")
    void signup_AutoPointsGrant() {
        // Given
        when(memberRepository.existsByMemberId("test123")).thenReturn(false);
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(pointHistoryRepository.save(any(PointHistory.class))).thenReturn(new PointHistory());

        // When
        MemberResponse response = memberService.signup(signupRequest);

        // Then
        assertEquals(1000L, response.getPoints());

        // Member 객체에 포인트가 추가되었는지 확인
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertEquals(1000L, savedMember.getPoints());

        // 포인트 히스토리에 적립 기록이 추가되었는지 확인
        ArgumentCaptor<PointHistory> pointCaptor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(pointCaptor.capture());
        PointHistory savedPointHistory = pointCaptor.getValue();
        assertEquals(PointHistory.PointType.EARN, savedPointHistory.getPointTypeEnum());
        assertEquals(1000L, savedPointHistory.getPointAmount());
    }

    /**
     * 테스트용 적립금 내역 데이터 생성
     */
    private List<PointHistory> createTestPointHistories() {
        // 회원가입 적립금 (가장 오래된 기록)
        PointHistory signupPoints = new PointHistory(testMember, PointHistory.PointType.EARN, 1000L, null);
        signupPoints.setId(1L);
        signupPoints.setCreatedAt(LocalDateTime.now().minusDays(2));

        // 적립금 사용 기록
        PointHistory usePoints = new PointHistory(testMember, PointHistory.PointType.USE, 300L, "ORDER_002");
        usePoints.setId(2L);
        usePoints.setCreatedAt(LocalDateTime.now().minusDays(1));

        // 적립금 적립 기록 (가장 최근)
        PointHistory earnPoints = new PointHistory(testMember, PointHistory.PointType.EARN, 500L, "ORDER_003");
        earnPoints.setId(3L);
        earnPoints.setCreatedAt(LocalDateTime.now());

        // 최신순 정렬된 리스트 반환 (실제 repository에서 반환될 형태)
        return List.of(earnPoints, usePoints, signupPoints);
    }
}