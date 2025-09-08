package com.example.payment.repository;

import com.example.payment.entity.Member;
import com.example.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByOrderId(String orderId);
    
    List<Payment> findByMemberId(Long id);
    
    List<Payment> findByMemberOrderByPaymentAtDesc(Member member);
    
    /**
     * 주문 ID로 TID가 있는 결제 조회 (취소용)
     * payments 테이블의 모든 데이터는 성공한 결제이므로 status 체크 불필요
     */
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.tid IS NOT NULL")
    Payment findTidByOrderId(@Param("orderId") String orderId);
}