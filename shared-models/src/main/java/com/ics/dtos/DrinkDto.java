package com.ics.dtos;

import com.ics.models.Branch; // Make sure to import the Branch enum
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Use NoArgsConstructor for flexibility

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrinkDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String drinkName;
    private int drinkQuantity;
    private double drinkPrice;
    private Branch branch;
}
