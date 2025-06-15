package com.ics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {
    private long drinkId;
    private int quantity;
}
