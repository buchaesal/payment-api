package com.example.payment.service;

import com.example.payment.entity.InterfaceHistory;
import com.example.payment.repository.InterfaceHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterfaceHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InterfaceHistoryService.class);
    
    private final InterfaceHistoryRepository interfaceHistoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * API 요청 시작시 이력 생성
     */
    public InterfaceHistory createRequestHistory(String interfaceType, String apiName, String requestUrl, Object requestData, String orderId) {
        try {
            String requestJson = convertToJson(requestData);
            InterfaceHistory history = InterfaceHistory.createRequest(interfaceType, apiName, requestUrl, requestJson, orderId);
            InterfaceHistory saved = interfaceHistoryRepository.save(history);
            
            logger.info("인터페이스 요청 이력 생성: ID={}, Type={}, API={}, OrderId={}", 
                saved.getId(), interfaceType, apiName, orderId);
            
            return saved;
        } catch (Exception e) {
            logger.error("인터페이스 요청 이력 생성 실패: interfaceType={}, apiName={}", interfaceType, apiName, e);
            throw new RuntimeException("인터페이스 요청 이력 생성 실패", e);
        }
    }
    
    /**
     * API 응답 완료시 이력 업데이트
     */
    public void updateResponseHistory(Long historyId, Object responseData, String responseCode, Integer httpStatus, String errorMessage) {
        try {
            InterfaceHistory history = interfaceHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("인터페이스 이력을 찾을 수 없습니다: " + historyId));
            
            String responseJson = convertToJson(responseData);
            history.completeResponse(responseJson, responseCode, httpStatus, errorMessage);
            
            interfaceHistoryRepository.save(history);
            
            logger.info("인터페이스 응답 이력 업데이트: ID={}, ResponseCode={}, HttpStatus={}, ProcessingTime={}ms", 
                historyId, responseCode, httpStatus, history.getProcessingTimeMs());
            
        } catch (Exception e) {
            logger.error("인터페이스 응답 이력 업데이트 실패: historyId={}", historyId, e);
            // 이력 업데이트 실패는 원본 처리에 영향을 주지 않도록 예외를 던지지 않음
        }
    }
    
    /**
     * API 호출 성공시 이력 업데이트 (간편 메서드)
     */
    public void updateSuccessHistory(Long historyId, Object responseData, Integer httpStatus) {
        updateResponseHistory(historyId, responseData, "0000", httpStatus, null);
    }
    
    /**
     * API 호출 실패시 이력 업데이트 (간편 메서드)
     */
    public void updateFailureHistory(Long historyId, Object responseData, String responseCode, Integer httpStatus, String errorMessage) {
        if (responseCode == null || "0000".equals(responseCode)) {
            responseCode = "9999"; // 기본 실패 코드
        }
        updateResponseHistory(historyId, responseData, responseCode, httpStatus, errorMessage);
    }
    
    /**
     * 주문 ID로 인터페이스 이력 조회
     */
    @Transactional(readOnly = true)
    public List<InterfaceHistory> getHistoriesByOrderId(String orderId) {
        return interfaceHistoryRepository.findByOrderIdOrderByRequestTimeDesc(orderId);
    }
    
    /**
     * 인터페이스 타입별 이력 조회
     */
    @Transactional(readOnly = true)
    public List<InterfaceHistory> getHistoriesByInterfaceType(String interfaceType) {
        return interfaceHistoryRepository.findByInterfaceTypeOrderByRequestTimeDesc(interfaceType);
    }
    
    /**
     * 특정 기간 내 이력 조회
     */
    @Transactional(readOnly = true)
    public List<InterfaceHistory> getHistoriesByPeriod(LocalDateTime startTime, LocalDateTime endTime) {
        return interfaceHistoryRepository.findByRequestTimeBetween(startTime, endTime);
    }
    
    /**
     * 실패한 API 호출 이력 조회
     */
    @Transactional(readOnly = true)
    public List<InterfaceHistory> getFailedHistories() {
        return interfaceHistoryRepository.findFailedInterfaces();
    }
    
    /**
     * 객체를 JSON 문자열로 변환
     */
    private String convertToJson(Object data) {
        if (data == null) {
            return null;
        }
        
        if (data instanceof String) {
            return (String) data;
        }
        
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            logger.warn("JSON 변환 실패: {}", data.toString(), e);
            return data.toString();
        }
    }
}