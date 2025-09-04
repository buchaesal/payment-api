package com.example.payment.controller;

import com.example.payment.dto.MemberLoginRequest;
import com.example.payment.dto.MemberSignupRequest;
import com.example.payment.dto.MemberResponse;
import com.example.payment.dto.PointHistoryResponse;
import com.example.payment.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = "*")
public class MemberController {
    
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    
    @Autowired
    private MemberService memberService;
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody MemberSignupRequest request) {
        logger.info("=== 회원가입 요청 ===");
        logger.info("회원 ID: {}", request.getMemberId());
        logger.info("이름: {}", request.getName());
        logger.info("이메일: {}", request.getEmail());
        logger.info("전화번호: {}", request.getPhone());
        
        try {
            MemberResponse memberResponse = memberService.signup(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "회원가입이 완료되었습니다. 가입축하 적립금 1,000원이 지급되었습니다.");
            response.put("member", memberResponse);
            
            logger.info("회원가입 성공: {}", memberResponse.getMemberId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            
            logger.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody MemberLoginRequest request) {
        logger.info("=== 로그인 요청 ===");
        logger.info("회원 ID: {}", request.getMemberId());
        
        try {
            MemberResponse memberResponse = memberService.login(request.getMemberId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "로그인 성공");
            response.put("member", memberResponse);
            
            logger.info("로그인 성공: {} (적립금: {}원)", memberResponse.getMemberId(), memberResponse.getPoints());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            
            logger.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{memberId}")
    public ResponseEntity<Map<String, Object>> getMember(@PathVariable String memberId) {
        logger.info("=== 회원 정보 조회 요청 ===");
        logger.info("회원 ID: {}", memberId);
        
        try {
            MemberResponse memberResponse = memberService.getMemberById(memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("member", memberResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{memberId}/points/history")
    public ResponseEntity<Map<String, Object>> getPointHistory(@PathVariable String memberId) {
        logger.info("=== 적립금 내역 조회 요청 ===");
        logger.info("회원 ID: {}", memberId);
        
        try {
            List<PointHistoryResponse> pointHistories = memberService.getPointHistory(memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("pointHistories", pointHistories);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}