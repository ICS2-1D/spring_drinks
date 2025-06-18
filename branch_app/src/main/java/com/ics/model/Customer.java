package com.ics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Customer_name", unique = true, nullable = false)
    private String customerName;

    @Column(name = "Customer_number", unique = true, nullable = false)
    private String customerNumber;

    // Note: Your schema doesn't have email. If you added it to DTO for future,
    // you'd need to add @Column(name = "customer_email") private String customerEmail; here too.
}
