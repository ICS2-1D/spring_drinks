package com.ics.distributed_drinks_system.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class OrderItemRequest {
    private int drinkId;
    private int quantity;
}
