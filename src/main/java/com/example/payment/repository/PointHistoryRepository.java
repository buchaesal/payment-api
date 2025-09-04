package com.example.payment.repository;

import com.example.payment.entity.PointHistory;
import com.example.payment.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    List<PointHistory> findByMemberOrderByCreatedAtDesc(Member member);
    
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}