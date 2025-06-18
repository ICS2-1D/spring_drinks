package com.ics.dtos;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PaymentResponse  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long paymentId;
    private Long orderId;
    private String customerNumber;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paymentTime;
}
