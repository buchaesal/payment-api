package com.example.payment.controller;

import com.example.payment.model.InicisFactory;
import com.example.payment.model.InicisPcPayInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inicis")
@CrossOrigin(origins = "*")
public class InicisController {
    
    private static final Logger logger = LoggerFactory.getLogger(InicisController.class);
    
    @GetMapping("/pc/pay-info")
    public ResponseEntity<Map<String, Object>> inicisPcPayInfo(@RequestParam String oid, @RequestParam String price) {
        logger.info("=== 이니시스 PC 결제 정보 조회 요청 ===");
        logger.info("주문번호(oid): {}", oid);
        logger.info("결제금액(price): {}", price);
        
        try {
            InicisPcPayInfo payInfo = InicisFactory.inicisPcPayInfo(oid, price);
            
            logger.info("이니시스 결제 정보 생성 완료");
            logger.info("MID: {}", payInfo.getMid());
            logger.info("Timestamp: {}", payInfo.getTimestamp());
            logger.info("mKey: {}", payInfo.getMKey());
            logger.info("Signature: {}", payInfo.getSignature());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("paymentInfo", payInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("이니시스 결제 정보 조회 중 오류 발생: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "결제 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("이니시스 컨트롤러 헬스체크 요청");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "이니시스 컨트롤러가 정상 작동중입니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
