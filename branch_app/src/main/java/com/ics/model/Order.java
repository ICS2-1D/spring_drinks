package com.ics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import com.ics.model.enums.Branch;
import com.ics.model.enums.OrderStatus;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", unique = true)
    private String orderNumber; // You'll need to generate this programmatically

    @Enumerated(EnumType.STRING) // Store enum as String in DB
    @Column(name = "branch", nullable = false)
    private Branch branch; // Assuming you create a Branch enum

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone_number", nullable = false)
    private String customerPhoneNumber;

    @Enumerated(EnumType.STRING) // Store enum as String in DB
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus; // Assuming you create an OrderStatus enum

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    // Additional field from DTO (if you add this column to your 'orders' table)
    // @Column(name = "delivery_address", nullable = false)
    // private String deliveryAddress;

    // If you choose to include special instructions in the orders table
    // @Column(name = "special_instructions", length = 1000)
    // private String specialInstructions;

    // One-to-Many relationship with OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    // One-to-One relationship with Payment (optional, or could be ManyToOne from Payment to Order)
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING; // Default status
        }
        if (orderNumber == null) {
            orderNumber = generateOrderNumber(); // Generate a unique order number
        }
    }

    // Simple placeholder for order number generation. Implement a more robust one.
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}