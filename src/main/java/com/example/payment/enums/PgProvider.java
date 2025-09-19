package com.example.payment.enums;

/**
 * PG사 제공업체 열거형
 * 지원하는 결제 게이트웨이 업체들을 정의
 */
public enum PgProvider {
    TOSS("토스페이먼츠"),
    INICIS("이니시스");

    private final String displayName;

    PgProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 문자열로부터 PgProvider 찾기 (대소문자 무시)
     */
    public static PgProvider fromString(String value) {
        if (value == null) {
            return null;
        }

        for (PgProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(value)) {
                return provider;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 PG사입니다: " + value);
    }
}