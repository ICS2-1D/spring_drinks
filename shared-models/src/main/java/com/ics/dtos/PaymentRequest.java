package com.ics.dtos;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private String customerNumber;
    private String paymentMethod;
    private String paymentStatus;
}
