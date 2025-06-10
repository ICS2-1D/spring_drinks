package com.ics.distributed_drinks_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrinkDto {
    private Long id;
    private String drinkName;
    private int drinkQuantity; // Available stock
    private double drinkPrice;
    private boolean available; // Computed field: drinkQuantity > 0
}