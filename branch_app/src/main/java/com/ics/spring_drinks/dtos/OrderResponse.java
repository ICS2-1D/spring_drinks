package com.ics.spring_drinks.dtos;

import com.ics.spring_drinks.models.Branch;
import com.ics.spring_drinks.models.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private CustomerDto customerDto;
    private Branch branch;
    private OrderStatus orderStatus;
    private Timestamp orderDate;
    private List<OrderItemResponse> items;
    private double totalAmount;
}
