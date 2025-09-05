package com.example.payment.controller;

import com.example.payment.dto.MemberLoginRequest;
import com.example.payment.dto.MemberSignupRequest;
import com.example.payment.dto.MemberResponse;
import com.example.payment.dto.MemberSignupResponse;
import com.example.payment.dto.MemberLoginResponse;
import com.example.payment.dto.MemberInfoResponse;
import com.example.payment.dto.PointHistoryListResponse;
import com.example.payment.dto.PointHistoryResponse;
import com.example.payment.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = "*")
public class MemberController {
    
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    
    @Autowired
    private MemberService memberService;
    
    @PostMapping("/signup")
    public MemberSignupResponse signup(@RequestBody MemberSignupRequest request) {
        logger.info("=== 회원가입 요청 ===");
        logger.info("회원 ID: {}", request.getMemberId());
        logger.info("이름: {}", request.getName());
        logger.info("이메일: {}", request.getEmail());
        logger.info("전화번호: {}", request.getPhone());
        
        try {
            MemberResponse memberResponse = memberService.signup(request);
            
            logger.info("회원가입 성공: {}", memberResponse.getMemberId());
            return new MemberSignupResponse(
                "SUCCESS",
                "회원가입이 완료되었습니다. 가입축하 적립금 1,000원이 지급되었습니다.",
                memberResponse
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("회원가입 실패: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public MemberLoginResponse login(@RequestBody MemberLoginRequest request) {
        logger.info("=== 로그인 요청 ===");
        logger.info("회원 ID: {}", request.getMemberId());
        
        try {
            MemberResponse memberResponse = memberService.login(request.getMemberId());
            
            logger.info("로그인 성공: {} (적립금: {}원)", memberResponse.getMemberId(), memberResponse.getPoints());
            return new MemberLoginResponse(
                "SUCCESS",
                "로그인 성공",
                memberResponse
            );
            
        } catch (IllegalArgumentException e) {
            logger.error("로그인 실패: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @GetMapping("/{memberId}")
    public MemberInfoResponse getMember(@PathVariable String memberId) {
        logger.info("=== 회원 정보 조회 요청 ===");
        logger.info("회원 ID: {}", memberId);
        
        try {
            MemberResponse memberResponse = memberService.getMemberById(memberId);
            
            return new MemberInfoResponse("SUCCESS", memberResponse);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @GetMapping("/{memberId}/points/history")
    public PointHistoryListResponse getPointHistory(@PathVariable String memberId) {
        logger.info("=== 적립금 내역 조회 요청 ===");
        logger.info("회원 ID: {}", memberId);
        
        try {
            List<PointHistoryResponse> pointHistories = memberService.getPointHistory(memberId);
            
            return new PointHistoryListResponse("SUCCESS", pointHistories);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}