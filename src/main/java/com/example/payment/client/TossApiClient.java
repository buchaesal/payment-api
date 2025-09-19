package com.example.payment.client;

import com.example.payment.config.TossConfig;
import com.example.payment.entity.InterfaceHistory;
import com.example.payment.service.InterfaceHistoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class TossApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TossApiClient.class);

    private final TossConfig tossConfig;
    private final InterfaceHistoryService interfaceHistoryService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public TossApiClient(TossConfig tossConfig, InterfaceHistoryService interfaceHistoryService) {
        this.tossConfig = tossConfig;
        this.interfaceHistoryService = interfaceHistoryService;
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
            .build();
    }
    
    /**
     * 토스 결제 승인 API 호출 (v1 API) - 인터페이스 이력 로깅 적용
     * @param paymentKey 결제 고유 키 (PayToken)
     * @param amount 결제 금액
     * @param orderId 주문 ID
     * @return 승인 결과
     * @throws RuntimeException API 호출 실패 시
     */
    public Map<String, Object> requestPaymentApproval(String paymentKey, Long amount, String orderId) {
        // 인터페이스 이력 시작
        InterfaceHistory history = null;
        
        try {
            // Base64 인증 헤더 생성 (apiKey + ':' 를 Base64로 인코딩)
            String authHeader = createAuthorizationHeader();
            
            // 요청 데이터 구성 (v1 API JSON 형식)
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("paymentKey", paymentKey);
            requestData.put("amount", amount);
            requestData.put("orderId", orderId);
            
            // 인터페이스 이력 생성 (요청 시작)
            history = interfaceHistoryService.createRequestHistory("TOSS", "confirm", tossConfig.getExecuteUrl(), requestData, orderId);
            final InterfaceHistory finalHistory = history;
            
            logger.info("토스 결제 승인 요청: paymentKey={}, amount={}, orderId={}, historyId={}", paymentKey, amount, orderId, history.getId());
            
            // WebClient를 사용한 비동기 HTTP 요청 (동기로 변환)
            Map<String, Object> result = webClient
                .post()
                .uri(tossConfig.getExecuteUrl())
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .bodyValue(requestData)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    Map<String, Object> parsedResult = parseResponse(responseBody);
                    logger.info("토스 결제 승인 성공: {}", parsedResult);
                    return parsedResult;
                })
                .onErrorMap(WebClientResponseException.class, ex -> {
                    String errorMessage = "토스 결제 승인 API 호출 실패: HTTP " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString();
                    logger.error(errorMessage);

                    // 실패 이력 업데이트를 별도 스레드에서 비동기로 실행
                    String responseCode = determineResponseCode(ex.getStatusCode().value());
                    Mono.fromRunnable(() -> interfaceHistoryService.updateFailureHistory(finalHistory.getId(), ex.getResponseBodyAsString(), responseCode, errorMessage))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();

                    return new RuntimeException(errorMessage);
                })
                .onErrorMap(Exception.class, ex -> {
                    String errorMessage = "토스 결제 승인 API 호출 중 오류 발생: " + ex.getMessage();
                    logger.error(errorMessage, ex);

                    // 실패 이력 업데이트를 별도 스레드에서 비동기로 실행
                    Mono.fromRunnable(() -> interfaceHistoryService.updateFailureHistory(finalHistory.getId(), null, "9999", errorMessage))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();

                    return new RuntimeException(errorMessage);
                })
                .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                .block();
            
            // 성공 이력 업데이트를 별도 스레드에서 비동기로 실행
            if (result != null) {
                final Map<String, Object> finalResult = result;
                final Long finalHistoryId = history.getId();
                Mono.fromRunnable(() -> interfaceHistoryService.updateSuccessHistory(finalHistoryId, finalResult))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            }
            
            return result;
            
        } catch (RuntimeException e) {
            // onErrorMap에서 생성된 RuntimeException을 다시 던짐
            throw e;
        } catch (Exception e) {
            String errorMessage = "토스 결제 승인 API 호출 중 예상치 못한 오류 발생: " + e.getMessage();
            logger.error(errorMessage, e);
            
            // 실패 이력 업데이트를 별도 스레드에서 비동기로 실행
            if (history != null) {
                final Long finalHistoryIdForCatch = history.getId();
                Mono.fromRunnable(() -> interfaceHistoryService.updateFailureHistory(finalHistoryIdForCatch, null, "9999", errorMessage))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            }
            
            throw new RuntimeException(errorMessage);
        }
    }
    
    /**
     * HTTP 상태 코드에 따른 응답 코드 결정
     */
    private String determineResponseCode(int httpStatus) {
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
     * 토스 결제 취소 API 호출 (v1 API) - 인터페이스 이력 로깅 적용
     * @param paymentKey 결제 고유 키 (PayToken)
     * @param cancelReason 취소 사유
     * @param orderId 주문 ID (로깅용)
     * @return 취소 결과
     * @throws RuntimeException API 호출 실패 시
     */
    public Map<String, Object> requestPaymentCancellation(String paymentKey, String cancelReason, String orderId) {
        // 인터페이스 이력 시작
        InterfaceHistory history = null;

        try {
            // Base64 인증 헤더 생성
            String authHeader = createAuthorizationHeader();

            // 환경별 설정에서 취소 URL을 가져와서 {paymentKey} placeholder 치환
            String cancelUrl = tossConfig.getCancelUrl().replace("{paymentKey}", paymentKey);

            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("cancelReason", cancelReason);

            // 인터페이스 이력 생성 (요청 시작)
            history = interfaceHistoryService.createRequestHistory("TOSS", "cancel", cancelUrl, requestData, orderId);
            final InterfaceHistory finalHistory = history;

            logger.info("토스 결제 취소 요청: paymentKey={}, cancelReason={}, orderId={}, historyId={}",
                       paymentKey, cancelReason, orderId, history.getId());

            // WebClient를 사용한 비동기 HTTP 요청 (동기로 변환)
            Map<String, Object> result = webClient
                .post()
                .uri(cancelUrl)
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .bodyValue(requestData)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    Map<String, Object> parsedResult = parseResponse(responseBody);
                    logger.info("토스 결제 취소 성공: {}", parsedResult);
                    return parsedResult;
                })
                .onErrorMap(WebClientResponseException.class, ex -> {
                    String errorMessage = "토스 결제 취소 API 호출 실패: HTTP " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString();
                    logger.error(errorMessage);

                    // 실패 이력 업데이트를 별도 스레드에서 비동기로 실행
                    String responseCode = determineResponseCode(ex.getStatusCode().value());
                    Mono.fromRunnable(() -> interfaceHistoryService.updateFailureHistory(finalHistory.getId(), ex.getResponseBodyAsString(), responseCode, errorMessage))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();

                    return new RuntimeException(errorMessage);
                })
                .onErrorMap(Exception.class, ex -> {
                    String errorMessage = "토스 결제 취소 API 호출 중 오류 발생: " + ex.getMessage();
                    logger.error(errorMessage, ex);

                    // 실패 이력 업데이트를 별도 스레드에서 비동기로 실행
                    Mono.fromRunnable(() -> interfaceHistoryService.updateFailureHistory(finalHistory.getId(), null, "9999", errorMessage))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();

                    return new RuntimeException(errorMessage);
                })
                .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                .block();

            // 성공 이력 업데이트를 별도 스레드에서 비동기로 실행
            if (result != null) {
                final Map<String, Object> finalResult = result;
                final Long finalHistoryId = history.getId();
                Mono.fromRunnable(() -> interfaceHistoryService.updateSuccessHistory(finalHistoryId, finalResult))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            }

            return result;

        } catch (RuntimeException e) {
            // onErrorMap에서 생성된 RuntimeException을 다시 던짐
            throw e;
        } catch (Exception e) {
            String errorMessage = "토스 결제 취소 API 호출 중 예상치 못한 오류 발생: " + e.getMessage();
            logger.error(errorMessage, e);

            // 실패 이력 업데이트를 별도 스레드에서 비동기로 실행
            if (history != null) {
                final Long finalHistoryIdForCatch = history.getId();
                Mono.fromRunnable(() -> interfaceHistoryService.updateFailureHistory(finalHistoryIdForCatch, null, "9999", errorMessage))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            }

            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Authorization 헤더 생성 (Basic 인증)
     * apiKey에 콜론을 붙인 후 Base64로 인코딩
     */
    private String createAuthorizationHeader() {
        String credentials = tossConfig.getApiKey() + ":";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
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