package com.ics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.ics.model.enums.PaymentMethod;
import com.ics.model.enums.PaymentStatus;


import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY) // One-to-one with Order
    @JoinColumn(name = "order_id", unique = true, nullable = false) // Ensures unique order_id for payment
    private Order order;

    @Column(name = "customer_number", nullable = false)
    private String customerNumber;

    @Enumerated(EnumType.STRING) // Store enum as String in DB
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod; // Assuming you create a PaymentMethod enum

    @Enumerated(EnumType.STRING) // Store enum as String in DB
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus; // Assuming you create a PaymentStatus enum

    @Column(name = "transaction_id", unique = true)
    private String transactionId; // From payment gateway

    @Column(name = "payment_time", nullable = false)
    private LocalDateTime paymentTime;

    @PrePersist
    protected void onCreate() {
        if (paymentTime == null) {
            paymentTime = LocalDateTime.now();
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING; // Default status
        }
    }
}