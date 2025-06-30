package com.ics.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor; // Add this
import lombok.Data;
import lombok.NoArgsConstructor; // Add this
// Removed lombok.RequiredArgsConstructor as AllArgsConstructor and NoArgsConstructor cover its needs for JPA
import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor // IMPORTANT: JPA requires a no-argument constructor for proxying
@AllArgsConstructor // Useful for creating instances with all fields, though setters are also used
@Entity
public class OrderItem implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false) // order_id must not be null
 private Order order;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "drink_id", referencedColumnName = "id", nullable = false) // drink_id must not be null
 private Drink drink;

 @Column(nullable = false) // Explicitly mark quantity as NOT NULL in DB schema
 private int quantity;

 @Column(nullable = false) // Explicitly mark unitPrice as NOT NULL in DB schema
 private double unitPrice;

 @Column(nullable = false) // Explicitly mark totalPrice as NOT NULL in DB schema
 private double totalPrice;
}
