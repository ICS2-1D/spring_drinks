package com.ics.spring_drinks.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private String drinkName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
