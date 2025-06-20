package com.ics.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "drinks")
public class Drink implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String drinkName;
    private int drinkQuantity;
    private double drinkPrice;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,mappedBy = "drink")
    @JsonIgnore
    private List<OrderItem> orderItems;
}
