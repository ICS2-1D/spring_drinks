package com.ics.spring_drinks.dtos;

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
}