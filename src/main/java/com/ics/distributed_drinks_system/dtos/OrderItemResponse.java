package com.ics.distributed_drinks_system.dtos;


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
