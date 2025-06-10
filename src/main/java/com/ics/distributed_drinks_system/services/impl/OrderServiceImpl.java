package com.ics.distributed_drinks_system.services.impl;

import com.ics.distributed_drinks_system.dtos.OrderRequest;
import com.ics.distributed_drinks_system.models.*;
import com.ics.distributed_drinks_system.repository.CustomerRepository;
import com.ics.distributed_drinks_system.repository.DrinkRepository;
import com.ics.distributed_drinks_system.repository.OrderItemRepository;
import com.ics.distributed_drinks_system.repository.OrderRepository;
import com.ics.distributed_drinks_system.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private DrinkRepository drinkRepository;
    private CustomerRepository customerRepository;
    private OrderItemRepository orderItemRepository;


    @Override
    public Order createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setBranch(request.getBranch());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(new Timestamp(System.currentTimeMillis()));

        order = orderRepository.save(order);

        // Process order items and associate them with the order
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (var itemRequest : request.getItems()) {
            Drink drink = drinkRepository.findById(itemRequest.getDrinkId())
                    .orElseThrow(() -> new RuntimeException("Drink not found"));

            if (drink.getDrinkQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for drink: " + drink.getDrinkName()
                        + ". Available: " + drink.getDrinkQuantity() + ", Requested: " + itemRequest.getQuantity());
            }


            // Create order item and associate it with the order
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setDrink(drink);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice((drink.getDrinkPrice() * itemRequest.getQuantity()));
            orderItem.setTotalPrice(drink.getDrinkPrice() * itemRequest.getQuantity());
            orderItems.add(orderItem);
            totalAmount += orderItem.getTotalPrice();

            // Update drink quantity in inventory
            drink.setDrinkQuantity(drink.getDrinkQuantity() - itemRequest.getQuantity());
            drinkRepository.save(drink);
        }
        // Save all order items
        orderItemRepository.saveAll(orderItems);

        //update order total amount and order items
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);


        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        // Generate a unique order number (e.g., using current timestamp or UUID)
        return "ORD-" + (int) (Math.random() * 1000);
    }

    @Override
    public List<Order> getOrdersByBranch(Branch branch) {
        return orderRepository.findByBranch(branch);
    }

    @Override
    public double calculateTotal(Order order) {
        double sum = order.getItems().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        return sum;
    }

    @Override
    @Transactional
    public void updateInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            Drink drink = item.getDrink();
            int newQuantity = drink.getDrinkQuantity() - item.getQuantity();
            if (newQuantity < 0) {
                throw new RuntimeException("Insufficient stock for drink: " + drink.getDrinkName());
            }
            drink.setDrinkQuantity(newQuantity);
            drinkRepository.save(drink);
        }

    }

    @Override
    @Transactional
    public void changeOrderStatus(int orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

    }
}
