package com.ics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "drinks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drink_name", unique = true, nullable = false)
    private String drinkName;

    @Column(name = "drink_quantity") // Assuming this is stock quantity
    private Integer drinkQuantity;

    @Column(name = "drink_price", nullable = false)
    private Double drinkPrice;
}