package com.ics.spring_drinks.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "drinks")
public class Drink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,name ="drink_name")
    private String drinkName;

    @Column(name = "drink_quantity")
    private int drinkQuantity;

    @Column(name = "drink_price")
    private double drinkPrice;

    @OneToMany(mappedBy = "drink", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItems;
}
