package com.ics.distributed_drinks_system.services.impl;

import com.ics.distributed_drinks_system.dtos.OrderRequest;
import com.ics.distributed_drinks_system.models.Branch;
import com.ics.distributed_drinks_system.models.Order;
import com.ics.distributed_drinks_system.models.OrderStatus;
import com.ics.distributed_drinks_system.services.OrderService;

import java.util.List;

public class OrderServiceImpl implements OrderService {
    @Override
    public Order createOrder(OrderRequest request) {
        return null;
    }

    @Override
    public double calculateTotal(Order order) {
        return 0;
    }

    @Override
    public void updateInventory(Order order) {

    }

    @Override
    public void changeOrderStatus(int orderId, OrderStatus newStatus) {

    }

    @Override
    public List<Order> getOrdersByBranch(Branch branch) {
        return List.of();
    }
}
