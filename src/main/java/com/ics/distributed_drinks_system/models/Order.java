package com.ics.distributed_drinks_system.models;

import java.sql.Timestamp;
import java.util.List;

public class Order {
    private int orderId;
    private List<OrderItem> items;
    private Branch branch;
    private Customer customer;
    private OrderStatus orderStatus;
    private Timestamp orderDate;
}
