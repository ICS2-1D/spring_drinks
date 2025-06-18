package com.ics.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String drinkName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
