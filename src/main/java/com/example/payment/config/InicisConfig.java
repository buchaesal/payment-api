package com.example.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "inicis")
public class InicisConfig {
    
    /**
     * 이니시스 상점 아이디
     * - 테스트: INIpayTest
     * - 운영: 실제 상점 ID로 교체 필요
     */
    private String mid;
    
    /**
     * 이니시스 서명 키 (Base64 인코딩)
     * - 테스트: SU5JTElURV9UUklQTEVERVNfS0VZRA==
     * - 운영: 실제 서명 키로 교체 필요
     */
    private String signKey;
    
    /**
     * 이니시스 API 키 (취소 API용)
     * - 테스트: ItEQKi3rY7uvDS8l
     * - 운영: 실제 API 키로 교체 필요
     */
    private String apiKey;
    
    /**
     * 문자셋 (기본: UTF-8)
     */
    private String charset;
    
    /**
     * 응답 포맷 (기본: JSON)
     */
    private String format;
}