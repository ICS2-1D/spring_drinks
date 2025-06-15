package com.ics.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String customerNumber;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paymentTime;
}
