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
@Table(name = "orders")
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderId;

    @Column(unique = true, name = "order_number")
    private String orderNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    @Column(name = "branch")
    private Branch branch;

    @Embedded
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "order_date")
    private Timestamp orderDate;

    @Column(name = "total_amount")
    private double totalAmount;
}
