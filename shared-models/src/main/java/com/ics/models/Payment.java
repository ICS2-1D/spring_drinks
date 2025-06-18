package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Entity
public class Payment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    private Long paymentId;
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    private String customerNumber;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paymentTime;
}

