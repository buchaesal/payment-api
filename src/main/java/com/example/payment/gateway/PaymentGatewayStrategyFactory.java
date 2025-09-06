package com.example.payment.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayStrategyFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayStrategyFactory.class);
    
    @Autowired
    private List<PaymentGatewayStrategy> gatewayStrategies;
    
    /**
     * PG 구분코드를 기반으로 적절한 PG 전략을 선택
     * @param pgProvider PG 구분코드 (TOSS, INICIS 등)
     * @return 해당하는 PG 전략
     */
    public PaymentGatewayStrategy getStrategy(String pgProvider) {
        logger.info("=== PG 전략 선택 시작 ===");
        logger.info("PG 구분코드: {}", pgProvider);
        
        if (pgProvider == null || pgProvider.trim().isEmpty()) {
            logger.warn("PG 구분코드가 없어 기본 전략 사용");
            // 기본적으로 토스페이먼츠 전략 반환 (첫 번째로 찾은 토스 전략)
            return gatewayStrategies.stream()
                    .filter(strategy -> strategy instanceof TossPaymentGatewayStrategy)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("기본 PG 전략을 찾을 수 없습니다"));
        }
        
        for (PaymentGatewayStrategy strategy : gatewayStrategies) {
            if (strategy.supports(pgProvider)) {
                logger.info("선택된 PG 전략: {}", strategy.getClass().getSimpleName());
                logger.info("=== PG 전략 선택 완료 ===");
                return strategy;
            }
        }
        
        logger.error("지원되지 않는 PG 구분코드: {}", pgProvider);
        throw new IllegalArgumentException("지원되지 않는 PG 구분코드입니다: " + pgProvider);
    }
    
    /**
     * 사용 가능한 모든 PG 전략 목록 반환
     * @return PG 전략 목록
     */
    public List<PaymentGatewayStrategy> getAllStrategies() {
        return gatewayStrategies;
    }
}