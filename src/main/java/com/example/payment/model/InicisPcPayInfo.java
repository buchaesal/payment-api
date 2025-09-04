package com.example.payment.model;

import lombok.Value;

@Value
public class InicisPcPayInfo {

    String mid;
    String signKey;
    String timestamp;
    String mKey;
    String signature;

}
