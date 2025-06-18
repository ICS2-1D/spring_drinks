package com.ics.dtos;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PaymentRequest  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String customerNumber;
    private String paymentMethod;
    private String paymentStatus;
}
