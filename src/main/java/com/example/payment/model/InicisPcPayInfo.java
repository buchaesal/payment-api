package com.example.payment.model;

public class InicisPcPayInfo {

    private final String mid;
    private final String signKey;
    private final String timestamp;
    private final String mKey;
    private final String signature;
    private final String verification;

    public InicisPcPayInfo(String mid, String signKey, String timestamp, String mKey, String signature, String verification) {
        this.mid = mid;
        this.signKey = signKey;
        this.timestamp = timestamp;
        this.mKey = mKey;
        this.signature = signature;
        this.verification = verification;
    }

    public String getMid() {
        return mid;
    }

    public String getSignKey() {
        return signKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMKey() {
        return mKey;
    }

    public String getSignature() {
        return signature;
    }

    public String getVerification() { return verification; }
}
