package com.ics.spring_drinks.services;

import com.ics.dtos.PaymentRequest;
import com.ics.dtos.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
}
