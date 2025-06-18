package com.ics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private long drinkId;
    private int quantity;
}
