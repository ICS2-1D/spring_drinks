package com.ics.dtos;

import com.ics.models.Branch;
import com.ics.models.OrderStatus;
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
