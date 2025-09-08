package com.example.payment.client;

import com.example.payment.config.InicisConfig;
import com.example.payment.entity.InterfaceHistory;
import com.example.payment.service.InterfaceHistoryService;
import com.example.payment.util.CryptoUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class InicisApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(InicisApiClient.class);

    private final InicisConfig inicisConfig;
    private final InterfaceHistoryService interfaceHistoryService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public InicisApiClient(InicisConfig inicisConfig, InterfaceHistoryService interfaceHistoryService) {
        this.inicisConfig = inicisConfig;
        this.interfaceHistoryService = interfaceHistoryService;
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
            .build();
    }
    
    /**
     * 이니시스 결제 승인 API 호출 - 인터페이스 이력 로깅 적용
     * @param authUrl 승인요청 URL
     * @param authToken 승인요청 검증 토큰
     * @param orderId 주문 ID (이력 관리용)
     * @return 승인 결과
     * @throws RuntimeException API 호출 실패 시
     */
    public Map<String, Object> requestPaymentApproval(String authUrl, String authToken, String orderId) {
        try {
            // 타임스탬프 생성
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // 해시 생성
            String signature = CryptoUtil.generateInicisSignature(authToken, timestamp);
            String verification = CryptoUtil.generateInicisVerification(authToken, inicisConfig.getSignKey(), timestamp);
            
            // 요청 파라미터 구성 (form-data 형식)
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("mid", inicisConfig.getMid());
            formData.add("authToken", authToken);
            formData.add("timestamp", timestamp);
            formData.add("signature", signature);
            formData.add("verification", verification);
            formData.add("charset", inicisConfig.getCharset());
            formData.add("format", inicisConfig.getFormat());
            
            // 인터페이스 이력 생성 (요청 시작)
            Map<String, Object> requestDataForHistory = convertFormDataToMap(formData);
            InterfaceHistory history = interfaceHistoryService.createRequestHistory("INICIS", "confirm", authUrl, requestDataForHistory, orderId);
            final Long historyId = history.getId(); // final로 만들어 람다에서 사용 가능
            
            logger.info("이니시스 결제 승인 요청: authToken={}, orderId={}, historyId={}", authToken, orderId, historyId);
            logger.info("POST 데이터: {}", formData);
            
            // WebClient를 사용한 비동기 HTTP 요청 (동기로 변환)
            Map<String, Object> result = webClient
                .post()
                .uri(authUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    logger.info("이니시스 API 응답: {}", responseBody);
                    Map<String, Object> parsedResult = parseResponse(responseBody);
                    parsedResult.put("httpStatus", 200);
                    logger.info("=== 이니시스 승인 API 호출 성공 ===");
                    return parsedResult;
                })
                .onErrorMap(WebClientResponseException.class, ex -> {
                    String errorMessage = "이니시스 결제 승인 API 호출 실패: HTTP " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString();
                    logger.error(errorMessage);
                    
                    // 실패 이력 업데이트
                    String responseCode = determineInicisResponseCode(ex.getStatusCode().value());
                    interfaceHistoryService.updateFailureHistory(historyId, ex.getResponseBodyAsString(), responseCode, ex.getStatusCode().value(), errorMessage);
                    
                    return new RuntimeException(errorMessage);
                })
                .onErrorMap(Exception.class, ex -> {
                    String errorMessage = "이니시스 결제 승인 API 호출 중 오류 발생: " + ex.getMessage();
                    logger.error(errorMessage, ex);
                    
                    // 실패 이력 업데이트
                    interfaceHistoryService.updateFailureHistory(historyId, null, "9999", null, errorMessage);
                    
                    return new RuntimeException(errorMessage);
                })
                .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                .block();
            
            // 성공 이력 업데이트
            if (result != null) {
                Integer httpStatus = (Integer) result.get("httpStatus");
                // 이니시스 응답에서 결과 코드 확인 (resultCode 등)
                String inicisResultCode = determineInicisResultCode(result);
                interfaceHistoryService.updateResponseHistory(historyId, result, inicisResultCode, httpStatus, null);
            }
            
            return result;
            
        } catch (RuntimeException e) {
            // onErrorMap에서 생성된 RuntimeException을 다시 던짐
            throw e;
        } catch (Exception e) {
            String errorMessage = "이니시스 API 호출 중 예상치 못한 오류 발생: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }
    
    /**
     * FormData를 Map으로 변환 (이력 저장용)
     */
    private Map<String, Object> convertFormDataToMap(MultiValueMap<String, String> formData) {
        Map<String, Object> result = new HashMap<>();
        formData.forEach((key, values) -> {
            if (values.size() == 1) {
                result.put(key, values.get(0));
            } else {
                result.put(key, values);
            }
        });
        return result;
    }
    
    /**
     * HTTP 상태 코드에 따른 이니시스 응답 코드 결정
     */
    private String determineInicisResponseCode(int httpStatus) {
        if (httpStatus == 200) {
            return "0000";
        } else if (httpStatus >= 400 && httpStatus < 500) {
            return "4000"; // 클라이언트 오류
        } else if (httpStatus >= 500) {
            return "5000"; // 서버 오류
        } else {
            return "9999"; // 기타 오류
        }
    }
    
    /**
     * 이니시스 응답에서 실제 결과 코드 추출
     */
    private String determineInicisResultCode(Map<String, Object> result) {
        // 이니시스 응답에서 resultCode 또는 유사한 필드 확인
        Object resultCode = result.get("resultCode");
        if (resultCode != null) {
            String code = resultCode.toString();
            return "0000".equals(code) ? "0000" : code;
        }
        
        // 다른 가능한 필드명들 확인
        Object code = result.get("code");
        if (code != null && "0000".equals(code.toString())) {
            return "0000";
        }
        
        // HTTP 상태가 200이면 성공으로 간주
        Object httpStatus = result.get("httpStatus");
        if (httpStatus != null && httpStatus.equals(200)) {
            return "0000";
        }
        
        return "9999"; // 기본값
    }
    
    /**
     * 이니시스 결제 취소 API 호출 - 인터페이스 이력 로깅 적용
     * @param tid 취소요청할 승인TID
     * @param orderId 주문 ID (이력 관리용)
     * @return 취소 결과
     * @throws RuntimeException API 호출 실패 시
     */
    public Map<String, Object> requestPaymentCancel(String tid, String orderId) {
        final String REFUND_URL = "https://iniapi.inicis.com/v2/pg/refund";
        
        try {
            // 타임스탬프 생성 (YYYYMMDDhhmmss 형식)
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            // 요청 데이터 구성
            Map<String, Object> data = new HashMap<>();
            data.put("tid", tid);
            data.put("msg", "결제취소");
            
            String dataJson = objectMapper.writeValueAsString(data);
            
            // hashData 생성 (INIAPIKey + mid + type + timestamp + data)
            String hashData = CryptoUtil.generateInicisRefundHashData(
                inicisConfig.getApiKey(), 
                inicisConfig.getMid(), 
                "refund", 
                timestamp, 
                dataJson
            );
            
            // 최종 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("mid", inicisConfig.getMid());
            requestData.put("type", "refund");
            requestData.put("timestamp", timestamp);
            requestData.put("clientIp", "127.0.0.1");
            requestData.put("hashData", hashData);
            requestData.put("data", data);
            
            // 인터페이스 이력 생성 (요청 시작)
            InterfaceHistory history = interfaceHistoryService.createRequestHistory("INICIS", "cancel", REFUND_URL, requestData, orderId);
            final Long historyId = history.getId(); // final로 만들어 람다에서 사용 가능
            
            logger.info("이니시스 결제 취소 요청: tid={}, orderId={}, historyId={}", tid, orderId, historyId);
            logger.info("취소 요청 데이터: {}", requestData);
            
            // WebClient를 사용한 비동기 HTTP 요청 (동기로 변환)
            Map<String, Object> result = webClient
                .post()
                .uri(REFUND_URL)
                .header("Content-Type", "application/json")
                .bodyValue(requestData)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    logger.info("이니시스 취소 API 응답: {}", responseBody);
                    Map<String, Object> parsedResult = parseResponse(responseBody);
                    parsedResult.put("httpStatus", 200);
                    logger.info("=== 이니시스 취소 API 호출 성공 ===");
                    return parsedResult;
                })
                .onErrorMap(WebClientResponseException.class, ex -> {
                    String errorMessage = "이니시스 결제 취소 API 호출 실패: HTTP " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString();
                    logger.error(errorMessage);
                    
                    // 실패 이력 업데이트
                    String responseCode = determineInicisResponseCode(ex.getStatusCode().value());
                    interfaceHistoryService.updateFailureHistory(historyId, ex.getResponseBodyAsString(), responseCode, ex.getStatusCode().value(), errorMessage);
                    
                    return new RuntimeException(errorMessage);
                })
                .onErrorMap(Exception.class, ex -> {
                    String errorMessage = "이니시스 결제 취소 API 호출 중 오류 발생: " + ex.getMessage();
                    logger.error(errorMessage, ex);
                    
                    // 실패 이력 업데이트
                    interfaceHistoryService.updateFailureHistory(historyId, null, "9999", null, errorMessage);
                    
                    return new RuntimeException(errorMessage);
                })
                .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                .block();
            
            // 성공 이력 업데이트
            if (result != null) {
                Integer httpStatus = (Integer) result.get("httpStatus");
                // 이니시스 취소 응답에서 결과 코드 확인
                String inicisResultCode = determineInicisResultCode(result);
                interfaceHistoryService.updateResponseHistory(historyId, result, inicisResultCode, httpStatus, null);
            }
            
            return result;
            
        } catch (RuntimeException e) {
            // onErrorMap에서 생성된 RuntimeException을 다시 던짐
            throw e;
        } catch (Exception e) {
            String errorMessage = "이니시스 취소 API 호출 중 예상치 못한 오류 발생: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
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