package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Entity
public class OrderItem implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;
 @Id
 private Long id;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "order_id", referencedColumnName = "orderId")
 private Order order;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "drink_id", referencedColumnName = "id")
 private Drink drink;

 private int quantity;
 private double unitPrice;
 private double totalPrice;
}
