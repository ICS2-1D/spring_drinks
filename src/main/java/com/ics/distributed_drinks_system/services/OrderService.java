package com.ics.distributed_drinks_system.services;

import com.ics.distributed_drinks_system.dtos.OrderRequest;
import com.ics.distributed_drinks_system.models.Branch;
import com.ics.distributed_drinks_system.models.Order;
import com.ics.distributed_drinks_system.models.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequest request);
    double calculateTotal(Order order);
    void updateInventory(Order order);
    void changeOrderStatus(int orderId, OrderStatus newStatus);
    List<Order> getOrdersByBranch(Branch branch);
}
