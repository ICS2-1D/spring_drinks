package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@RequiredArgsConstructor
@Entity
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    private long orderId;
    private String orderNumber;
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    private Branch branch;

    @Embedded
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private Timestamp orderDate;
    private double totalAmount;
}
