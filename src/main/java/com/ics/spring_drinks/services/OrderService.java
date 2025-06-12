package com.ics.spring_drinks.services;

import com.ics.spring_drinks.dtos.OrderRequest;
import com.ics.spring_drinks.models.Branch;
import com.ics.spring_drinks.models.Order;
import com.ics.spring_drinks.models.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {

    Order createOrder(OrderRequest request);
    double calculateTotal(long orderId);
    void changeOrderStatusAndUpdateInventory(int orderId, OrderStatus newStatus);
//    List<Order> getOrdersByBranch(Branch branch);
}
