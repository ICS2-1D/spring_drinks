package com.ics.dtos;

import com.ics.models.Branch;
import com.ics.models.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;


@Data
@RequiredArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private String customerName;
    private String customerPhoneNumber;
    private Branch branch;
    private OrderStatus orderStatus;
    private Timestamp orderDate;
    private List<OrderItemResponse> items;
    private double totalAmount;
}
