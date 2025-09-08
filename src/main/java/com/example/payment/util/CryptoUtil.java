package com.example.payment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
    
    /**
     * SHA256 해시 생성
     * @param data 해시할 데이터
     * @return SHA256 해시 문자열 (소문자)
     */
    public static String generateSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String result = hexString.toString();
            logger.debug("SHA256 해시 생성 - 원본: {}, 해시: {}", data, result);
            return result;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 알고리즘을 찾을 수 없습니다: {}", e.getMessage());
            throw new RuntimeException("SHA-256 해시 생성 실패", e);
        }
    }
    
    /**
     * SHA512 해시 생성
     * @param data 해시할 데이터
     * @return SHA512 해시 문자열 (소문자)
     */
    public static String generateSHA512(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String result = hexString.toString();
            logger.debug("SHA512 해시 생성 - 원본: {}, 해시: {}", data, result);
            return result;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-512 알고리즘을 찾을 수 없습니다: {}", e.getMessage());
            throw new RuntimeException("SHA-512 해시 생성 실패", e);
        }
    }
    
    /**
     * 이니시스 signature 생성
     * @param authToken 승인요청 검증 토큰
     * @param timestamp 타임스탬프
     * @return signature 값
     */
    public static String generateInicisSignature(String authToken, String timestamp) {
        String data = "authToken=" + authToken + "&timestamp=" + timestamp;
        return generateSHA256(data);
    }
    
    /**
     * 이니시스 verification 생성
     * @param authToken 승인요청 검증 토큰
     * @param signKey signKey
     * @param timestamp 타임스탬프
     * @return verification 값
     */
    public static String generateInicisVerification(String authToken, String signKey, String timestamp) {
        String data = "authToken=" + authToken + "&signKey=" + signKey + "&timestamp=" + timestamp;
        return generateSHA256(data);
    }
    
    /**
     * 이니시스 취소 API 해시 데이터 생성 (SHA512)
     * @param iniAPIKey 이니시스 API 키
     * @param mid 상점아이디
     * @param type 요청서비스 타입 ("refund")
     * @param timestamp 전문생성시간
     * @param data 요청데이터 (JSON 문자열)
     * @return SHA512 해시 값
     */
    public static String generateInicisRefundHashData(String iniAPIKey, String mid, String type, String timestamp, String data) {
        String hashTarget = iniAPIKey + mid + type + timestamp + data;
        logger.debug("이니시스 취소 해시 대상: {}", hashTarget);
        return generateSHA512(hashTarget);
    }
}