package com.ics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DrinkDto  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String drinkName;
    private int drinkQuantity;
    private double drinkPrice;
}