package com.example.payment.repository;

import com.example.payment.entity.InterfaceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterfaceHistoryRepository extends JpaRepository<InterfaceHistory, Long> {
    
    /**
     * 주문 ID로 인터페이스 이력 조회
     */
    List<InterfaceHistory> findByOrderIdOrderByRequestTimeDesc(String orderId);
    
    /**
     * 인터페이스 타입별 이력 조회
     */
    List<InterfaceHistory> findByInterfaceTypeOrderByRequestTimeDesc(String interfaceType);
    
    /**
     * 특정 기간 내 이력 조회
     */
    @Query("SELECT ih FROM InterfaceHistory ih WHERE ih.requestTime BETWEEN :startTime AND :endTime ORDER BY ih.requestTime DESC")
    List<InterfaceHistory> findByRequestTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 응답 코드별 이력 조회
     */
    List<InterfaceHistory> findByResponseCodeOrderByRequestTimeDesc(String responseCode);
    
    /**
     * 실패한 API 호출 이력 조회 (응답 코드가 0000이 아닌 것들)
     */
    @Query("SELECT ih FROM InterfaceHistory ih WHERE ih.responseCode != '0000' OR ih.responseCode IS NULL ORDER BY ih.requestTime DESC")
    List<InterfaceHistory> findFailedInterfaces();
    
    /**
     * 특정 인터페이스 타입과 API 이름으로 최근 이력 조회
     */
    InterfaceHistory findFirstByInterfaceTypeAndApiNameOrderByRequestTimeDesc(String interfaceType, String apiName);
}