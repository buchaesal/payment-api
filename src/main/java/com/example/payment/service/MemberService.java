package com.example.payment.service;

import com.example.payment.entity.Member;
import com.example.payment.entity.PointHistory;
import com.example.payment.repository.MemberRepository;
import com.example.payment.repository.PointHistoryRepository;
import com.example.payment.dto.MemberSignupRequest;
import com.example.payment.dto.MemberResponse;
import com.example.payment.dto.PointHistoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    
    public MemberResponse signup(MemberSignupRequest request) {
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new IllegalArgumentException("이미 존재하는 회원 ID입니다.");
        }
        
        if (request.getEmail() != null && memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        Member member = new Member(
            request.getMemberId(),
            request.getName(),
            request.getEmail(),
            request.getPhone()
        );
        
        member.addPoints(1000L);
        
        Member savedMember = memberRepository.save(member);
        
        PointHistory pointHistory = new PointHistory(
            savedMember,
            PointHistory.PointType.EARN,
            1000L,
            "회원가입 축하 적립금"
        );
        pointHistoryRepository.save(pointHistory);
        
        return new MemberResponse(
            savedMember.getId(),
            savedMember.getMemberId(),
            savedMember.getName(),
            savedMember.getEmail(),
            savedMember.getPhone(),
            savedMember.getPoints()
        );
    }
    
    public MemberResponse login(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        return new MemberResponse(
            member.getId(),
            member.getMemberId(),
            member.getName(),
            member.getEmail(),
            member.getPhone(),
            member.getPoints()
        );
    }
    
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        return new MemberResponse(
            member.getId(),
            member.getMemberId(),
            member.getName(),
            member.getEmail(),
            member.getPhone(),
            member.getPoints()
        );
    }
    
    @Transactional(readOnly = true)
    public List<PointHistoryResponse> getPointHistory(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        List<PointHistory> pointHistories = pointHistoryRepository.findByMemberOrderByCreatedAtDesc(member);
        
        return pointHistories.stream()
            .map(PointHistoryResponse::from)
            .collect(Collectors.toList());
    }
}