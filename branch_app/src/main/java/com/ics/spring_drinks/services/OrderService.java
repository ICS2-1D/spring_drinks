package com.ics.spring_drinks.services;

import com.ics.dtos.OrderRequest;
import com.ics.models.Order;
import com.ics.models.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    Order createOrder(OrderRequest request);
    double calculateTotal(long orderId);
    void changeOrderStatusAndUpdateInventory(int orderId, OrderStatus newStatus);
//    List<Order> getOrdersByBranch(Branch branch);
}
