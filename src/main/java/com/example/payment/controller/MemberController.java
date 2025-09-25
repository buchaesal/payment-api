package com.example.payment.controller;

import com.example.payment.dto.*;
import com.example.payment.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    
    @Autowired
    private MemberService memberService;
    
    @PostMapping(value = "/signup", produces = "application/json")
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
            return new MemberSignupResponse(
                "FAILURE",
                e.getMessage(),
                null
            );
        }
    }
    
    @PostMapping(value = "/login")
    public MemberResponse login(@RequestBody MemberLoginRequest request) {
        return memberService.login(request.getMemberId());
    }
    
    @GetMapping(value = "/{id}")
    public MemberResponse getMember(@PathVariable String id) {
        return memberService.getMemberById(id);
    }
    
    @GetMapping(value = "/{memberId}/points/history", produces = "application/json")
    public PointHistoryListResponse getPointHistory(@PathVariable String memberId) {
        logger.info("=== 적립금 내역 조회 요청 ===");
        logger.info("회원 ID: {}", memberId);
        
        try {
            List<PointHistoryResponse> pointHistories = memberService.getPointHistory(memberId);
            
            return new PointHistoryListResponse("SUCCESS", pointHistories);
            
        } catch (IllegalArgumentException e) {
            logger.error("적립금 내역 조회 실패: {}", e.getMessage());
            return new PointHistoryListResponse("FAILURE", null);
        }
    }
}