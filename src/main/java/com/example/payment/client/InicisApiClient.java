package com.example.payment.client;

import com.example.payment.config.InicisConfig;
import com.example.payment.util.CryptoUtil;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class InicisApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(InicisApiClient.class);
    
    @Autowired
    private InicisConfig inicisConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 이니시스 결제 승인 API 호출
     * @param authUrl 승인요청 URL
     * @param authToken 승인요청 검증 토큰
     * @return 승인 결과
     */
    public Map<String, Object> requestPaymentApproval(String authUrl, String authToken) {
        logger.info("=== 이니시스 승인 API 호출 시작 ===");
        logger.info("승인 URL: {}", authUrl);
        logger.info("authToken: {}", authToken);
        
        try {
            // 타임스탬프 생성
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // 해시 생성
            String signature = CryptoUtil.generateInicisSignature(authToken, timestamp);
            String verification = CryptoUtil.generateInicisVerification(authToken, inicisConfig.getSignKey(), timestamp);
            
            logger.info("승인 요청 파라미터:");
            logger.info("- mid: {}", inicisConfig.getMid());
            logger.info("- authToken: {}", authToken);
            logger.info("- timestamp: {}", timestamp);
            logger.info("- signature: {}", signature);
            logger.info("- verification: {}", verification);
            logger.info("- charset: {}", inicisConfig.getCharset());
            logger.info("- format: {}", inicisConfig.getFormat());
            
            // 요청 파라미터 구성
            StringBuilder postData = new StringBuilder();
            postData.append("mid=").append(URLEncoder.encode(inicisConfig.getMid(), StandardCharsets.UTF_8));
            postData.append("&authToken=").append(URLEncoder.encode(authToken, StandardCharsets.UTF_8));
            postData.append("&timestamp=").append(URLEncoder.encode(timestamp, StandardCharsets.UTF_8));
            postData.append("&signature=").append(URLEncoder.encode(signature, StandardCharsets.UTF_8));
            postData.append("&verification=").append(URLEncoder.encode(verification, StandardCharsets.UTF_8));
            postData.append("&charset=").append(URLEncoder.encode(inicisConfig.getCharset(), StandardCharsets.UTF_8));
            postData.append("&format=").append(URLEncoder.encode(inicisConfig.getFormat(), StandardCharsets.UTF_8));
            
            logger.info("POST 데이터: {}", postData.toString());
            
            // HTTP 요청 실행
            URL url = new URL(authUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postData.length()));
            connection.setDoOutput(true);
            
            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.toString().getBytes(StandardCharsets.UTF_8));
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
            logger.info("이니시스 API 응답: {}", responseBody);
            
            // JSON 응답 파싱
            Map<String, Object> result;
            if (responseCode >= 200 && responseCode < 300) {
                result = parseResponse(responseBody);
                result.put("httpStatus", responseCode);
                logger.info("=== 이니시스 승인 API 호출 성공 ===");
            } else {
                result = new HashMap<>();
                result.put("status", "FAILED");
                result.put("message", "HTTP 오류: " + responseCode + " - " + responseBody);
                result.put("httpStatus", responseCode);
                logger.error("이니시스 API 호출 실패: HTTP {}", responseCode);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("이니시스 API 호출 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "FAILED");
            errorResult.put("message", "API 호출 오류: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * 응답 파싱 (JSON 또는 다른 형식 처리)
     */
    private Map<String, Object> parseResponse(String responseBody) {
        try {
            // JSON 형식으로 파싱 시도
            return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("JSON 파싱 실패, 원문 응답으로 처리: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("rawResponse", responseBody);
            
            // 간단한 key=value 형식 파싱 시도 (NVP 형식)
            if (responseBody.contains("=") && responseBody.contains("&")) {
                String[] pairs = responseBody.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        result.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            
            return result;
        }
    }
}