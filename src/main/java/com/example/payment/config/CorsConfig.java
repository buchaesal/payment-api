package com.example.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 도메인 설정
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",          // 프론트엔드 개발 서버
            "https://localhost:3000",         // 프론트엔드 개발 서버 (HTTPS)
            "https://js.tosspayments.com",    // 토스페이먼츠 SDK 도메인
            "https://pay.toss.im",            // 토스페이먼츠 결제창 도메인
            "https://stg-pay.toss.im",        // 토스페이먼츠 스테이징 환경
            "https://api.tosspayments.com",   // 토스페이먼츠 API 도메인
            "https://stg-api.tosspayments.com", // 토스페이먼츠 스테이징 API
            "https://stdpay.inicis.com",      // 이니시스 표준결제창
            "https://mobile.inicis.com",      // 이니시스 모바일 결제창
            "https://iniweb.inicis.com",      // 이니시스 웹 결제창
            "https://iniapi.inicis.com",      // 이니시스 API 도메인
            "https://plugin.inicis.com"       // 이니시스 플러그인 도메인
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization",
            "X-Requested-With", "Cache-Control"
        ));

        // 자격 증명 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}