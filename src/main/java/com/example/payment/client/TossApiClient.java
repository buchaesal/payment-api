package com.example.payment.client;

import com.example.payment.config.TossConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class TossApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TossApiClient.class);
    
    @Autowired
    private TossConfig tossConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 토스 결제 승인 API 호출
     * @param payToken 결제 고유 번호 (인증 응답의 authToken)
     * @return 승인 결과
     */
    public Map<String, Object> requestPaymentApproval(String payToken) {
        logger.info("=== 토스 승인 API 호출 시작 ===");
        logger.info("실행 URL: {}", tossConfig.getExecuteUrl());
        logger.info("payToken: {}", payToken);
        
        try {
            // 요청 데이터 구성 (JSON 형식)
            Map<String, String> requestData = new HashMap<>();
            requestData.put("apiKey", tossConfig.getApiKey());
            requestData.put("payToken", payToken);
            
            String jsonRequestData = objectMapper.writeValueAsString(requestData);
            logger.info("요청 JSON 데이터: {}", jsonRequestData);
            
            // HTTP 요청 실행
            URL url = new URL(tossConfig.getExecuteUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(jsonRequestData.getBytes(StandardCharsets.UTF_8).length));
            connection.setDoOutput(true);
            
            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonRequestData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 응답 읽기
            int responseCode = connection.getResponseCode();
            logger.info("HTTP 응답 코드: {}", responseCode);
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 
                        ? connection.getInputStream() 
                        : connection.getErrorStream(), 
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            String responseBody = response.toString();
            logger.info("토스 API 응답: {}", responseBody);
            
            // JSON 응답 파싱
            Map<String, Object> result;
            if (responseCode >= 200 && responseCode < 300) {
                result = parseResponse(responseBody);
                result.put("httpStatus", responseCode);
                logger.info("=== 토스 승인 API 호출 성공 ===");
            } else {
                result = new HashMap<>();
                result.put("status", "FAILED");
                result.put("message", "HTTP 오류: " + responseCode + " - " + responseBody);
                result.put("httpStatus", responseCode);
                logger.error("토스 API 호출 실패: HTTP {}", responseCode);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("토스 API 호출 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "FAILED");
            errorResult.put("message", "API 호출 오류: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * JSON 응답 파싱
     */
    private Map<String, Object> parseResponse(String responseBody) {
        try {
            // JSON 형식으로 파싱 시도
            return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("JSON 파싱 실패, 원문 응답으로 처리: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("rawResponse", responseBody);
            return result;
        }
    }
}