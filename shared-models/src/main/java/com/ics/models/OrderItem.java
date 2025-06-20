package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 // Many order items belong to one order
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "order_id", referencedColumnName = "orderId")
 private Order order;

 // Many order items can reference one drink
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "drink_id", referencedColumnName = "id")
 private Drink drink;

 @Column(name = "quantity")
 private int quantity;

 @Column(name = "unit_price")
 private double unitPrice;

 @Column(name = "total_price")
 private double totalPrice;
}
