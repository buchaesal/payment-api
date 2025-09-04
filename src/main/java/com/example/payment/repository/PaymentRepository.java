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
    
    List<Payment> findByMemberId(String memberId);
    
    List<Payment> findByMemberOrderByPaymentAtDesc(Member member);
    
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = :status")
    List<Payment> findByOrderIdAndStatus(@Param("orderId") String orderId, 
                                        @Param("status") String status);
}