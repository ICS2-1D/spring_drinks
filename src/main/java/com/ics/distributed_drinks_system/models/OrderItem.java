package com.ics.distributed_drinks_system.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class OrderItem {
 private Drink drink;
}
