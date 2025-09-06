package com.example.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "inicis")
public class InicisConfig {
    
    private String mid = "INIpayTest";  // 테스트용 상점아이디
    private String signKey = "SU5JTElURV9UUklQTEVERVNfS0VZRA==";  // 테스트용 signKey
    private String charset = "UTF-8";
    private String format = "JSON";
    
    public String getMid() {
        return mid;
    }
    
    public void setMid(String mid) {
        this.mid = mid;
    }
    
    public String getSignKey() {
        return signKey;
    }
    
    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
}