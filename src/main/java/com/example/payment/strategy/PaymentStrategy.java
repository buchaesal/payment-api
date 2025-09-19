package com.example.payment.strategy;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentCancelRequest;
import com.example.payment.dto.PaymentProcessResult;
import com.example.payment.dto.PaymentCancelResult;

public interface PaymentStrategy {

    PaymentProcessResult processPayment(PaymentConfirmRequest request);

    PaymentCancelResult cancelPayment(PaymentCancelRequest request);
}