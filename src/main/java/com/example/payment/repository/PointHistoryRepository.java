package com.example.payment.repository;

import com.example.payment.entity.PointHistory;
import com.example.payment.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    List<PointHistory> findByMemberOrderByCreatedAtDesc(Member member);
    
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    
    // 주문번호와 포인트 타입으로 검색 (문자열 버전)
    Optional<PointHistory> findByOrderIdAndPointType(String orderId, String pointType);
    
    // 편의를 위한 enum 버전
    default Optional<PointHistory> findByOrderIdAndPointType(String orderId, PointHistory.PointType pointType) {
        return findByOrderIdAndPointType(orderId, pointType.name());
    }
    
    // 주문번호로 모든 포인트 히스토리 검색
    List<PointHistory> findByOrderIdOrderByCreatedAtDesc(String orderId);
}