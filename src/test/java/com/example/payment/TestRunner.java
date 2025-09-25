package com.example.payment;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 모든 테스트를 실행하기 위한 테스트 슈트
 *
 * 실행 방법:
 * 1. Maven: mvn test
 * 2. IDE에서 이 클래스를 우클릭하여 "Run TestRunner" 선택
 * 3. 개별 테스트: 각 테스트 클래스를 직접 실행
 */
@Suite
@SuiteDisplayName("Payment API 전체 테스트 슈트")
@SelectPackages({
    "com.example.payment.service",
    "com.example.payment.integration"
})
public class TestRunner {
    // JUnit 5 Suite로 모든 테스트를 통합 실행
}