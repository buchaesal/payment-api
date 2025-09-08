package com.example.payment.enums;

public enum PaymentType {
    APPROVE("승인"),
    CANCEL("취소");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}