package com.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TossPaymentApiApplication {

    public static void main(String[] args) {
        System.out.println("=== 토스페이먼츠 Spring Boot API 서버 시작 ===");
        SpringApplication.run(TossPaymentApiApplication.class, args);
        System.out.println("서버 주소: http://localhost:8080");
        System.out.println("헬스체크: http://localhost:8080/api/payment/health");
        System.out.println("결제 승인: POST http://localhost:8080/api/payment/confirm");
    }
}