package com.example.payment.model;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class InicisFactory {

    //TODO profile별 변경. 현재 테스트용
    private static final String MID = "INIpayTest";
    private static final String SIGN_KEY = "SU5JTElURV9UUklQTEVERVNfS0VZU1RS";
    private static final String API_KEY = "ItEQKi3rY7uvDS8l";
    private static final String CANCEL_PAYMENT_URL = "https://stginiapi.inicis.com/api/v1/refund";

    public static InicisPcPayInfo inicisPcPayInfo(String oid, String price) {
        throwIfEmpty(oid);
        throwIfEmpty(price);
        String timestamp = inicisTimestamp();
        String mKey = inicisMkey();
        String signature = inicisSignature(oid, price, timestamp);
        String verification = inicisVerification(oid, price, timestamp);
        return new InicisPcPayInfo(MID, SIGN_KEY, timestamp, mKey, signature, verification);
    }

    private static String inicisSignature(String oid, String price, String timestamp) {
        String nvpData = "oid=" + oid + "&price=" + price + "&timestamp=" + timestamp;
        return hashSha256(nvpData);
    }

    private static String inicisVerification(String oid, String price, String timestamp) {
        String nvpData = "oid=" + oid + "&price=" + price + "&signKey=" + SIGN_KEY + "&timestamp=" + timestamp;
        return hashSha256(nvpData);
    }

    private static String inicisMkey() {
        return hashSha256(SIGN_KEY);
    }

    private static String clientIp() {
        String clientIp;
        try {
            clientIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return clientIp;
    }

    private static String hashSha256(String data) {
        String result;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(data.getBytes(StandardCharsets.UTF_8));
            result = String.format("%064x", new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available");
        }
        return result;
    }

    private static String hashSha512(String shaBaseStr) {
        String result;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(shaBaseStr.getBytes(StandardCharsets.UTF_8));
            result = String.format("%0128x", new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("can not be happened");
        }
        return result;
    }

    public static String inicisTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static Map<String, String> pcPayApproveReq(Map<String, String> payReturn, String timestamp) {
        Map<String, String> requestMap = new Hashtable<>();
        requestMap.put("mid", MID);
        requestMap.put("authToken", payReturn.get("authToken"));
        requestMap.put("signature", pcSignature(payReturn.get("authToken"), timestamp));
        requestMap.put("timestamp", timestamp);
        requestMap.put("charset", "UTF-8");
        requestMap.put("format", "JSON");
        return requestMap;
    }

    private static String pcSignature(String authToken, String timestamp) {
        String nvpData = "authToken=" + authToken + "&signKey=" + SIGN_KEY + "&timestamp=" + timestamp;
        return hashSha256(nvpData);
    }

    public static String pcSecureSignature(String timestamp, Map<String, String> approveResult) {
        String nvpData = "mid=" + MID + "&signKey=" + SIGN_KEY + "&tstamp=" + timestamp + 
                        "&MOID=" + approveResult.get("MOID") + "&TotPrice=" + approveResult.get("TotPrice");
        return hashSha256(nvpData);
    }

    public static Map<String, String> pcNetCancelReq(Map<String, String> payReturn, String timestamp) {
        return pcPayApproveReq(payReturn, timestamp);
    }

    public static String moPayUrlWithParams(Map<String, String> payReturn) {
        return payReturn.get("P_REQ_URL") + "?P_TID=" + payReturn.get("P_TID") + "&P_MID=" + MID;
    }

//    public static InicisMoPayInfo inicisMoPayInfo() {
//        return new InicisMoPayInfo(MID);
//    }

    public static MultiValueMap<String, String> moNetCancelRequest(String P_TID, String P_AMT, String P_OID) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("P_TID", P_TID);
        request.add("P_MID", MID);
        request.add("P_AMT", P_AMT);
        request.add("P_OID", P_OID);
        return request;
    }

    public static String moNetCancelUrl(String approveReqUrl) {
        String result;
        try {
            URL url = new URL(approveReqUrl);
            result = "https://" + url.getHost() + "/smart/payNetCancel.ini";
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static MultiValueMap<String, String> inicisCancelRequest(String payMethod, String tid) {
        String type = "Refund";
        String msg = "취소요청";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        String clientIp = clientIp();
        String hashData = hashSha512(appendString(API_KEY, type, payMethod, timestamp, clientIp, MID, tid));
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        result.add("type", type);
        result.add("paymethod", payMethod);
        result.add("timestamp", timestamp);
        result.add("clientIp", clientIp);
        result.add("mid", MID);
        result.add("tid", tid);
        result.add("msg", msg);
        result.add("hashData", hashData);
        return result;
    }

    public static MultiValueMap<String, String> inicisPartialCancelRequest(String payMethod, String tid, String cancelPrice, String confirmPrice) {
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        String type = "PartialRefund";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        String clientIp = clientIp();
        String msg = "부분취소요청";
        req.add("type", type);
        req.add("paymethod", payMethod);
        req.add("timestamp", timestamp);
        req.add("clientIp", clientIp);
        req.add("mid", MID);
        req.add("tid", tid);
        req.add("msg", msg);
        req.add("price", cancelPrice);
        req.add("confirmPrice", confirmPrice);
        String hashBaseStr = appendString(API_KEY, type, payMethod, timestamp, clientIp, MID, tid, cancelPrice, confirmPrice);
        req.add("hashData", hashSha512(hashBaseStr));
        return req;
    }

    private static String appendString(String ...strs){
        StringBuilder builder = new StringBuilder();
        for (String str : strs) {
            builder.append(str);
        }
        return builder.toString();
    }

    public static String cancelPaymentUrl() {
        return CANCEL_PAYMENT_URL;
    }

    public static String partialCancelPaymentUrl() {
        return cancelPaymentUrl();
    }

    private static void throwIfEmpty(String param) {
        if (!StringUtils.hasText(param)) {
            throw new RuntimeException("param is empty");
        }
    }

}
