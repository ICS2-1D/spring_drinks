package com.ics.spring_drinks.services.impl;

import com.ics.dtos.OrderItemResponse;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.models.*;
import com.ics.spring_drinks.repository.*;
import com.ics.spring_drinks.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DrinkRepository drinkRepository;
    private final CustomerRepository customerRepository;
    private final BranchStockRepository branchStockRepository;
    private final RestockRequestRepository restockRequestRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Customer customer = customerRepository.findByCustomerPhoneNumber(request.getCustomerPhoneNumber())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomer_name(request.getCustomerName());
                    newCustomer.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
                    return customerRepository.save(newCustomer);
                });

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderNumber(generateOrderNumber());
        order.setBranch(request.getBranch());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));

        List<OrderItem> orderItems = new ArrayList<>();
        double totalOrderAmount = 0.0;

        for (var itemRequest : request.getItems()) {
            Drink drink = drinkRepository.findById(itemRequest.getDrinkId())
                    .orElseThrow(() -> new RuntimeException("Drink with ID " + itemRequest.getDrinkId() + " not found."));

            BranchStock branchStock = branchStockRepository.findByBranchAndDrink(request.getBranch(), drink)
                    .orElseThrow(() -> new RuntimeException("Drink not available at this branch"));

            if (branchStock.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for drink: " + drink.getDrinkName()
                        + ". Available: " + branchStock.getQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setDrink(drink);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(drink.getDrinkPrice());
            orderItem.setTotalPrice(drink.getDrinkPrice() * itemRequest.getQuantity());

            orderItems.add(orderItem);
            totalOrderAmount += orderItem.getTotalPrice();

            branchStock.setQuantity(branchStock.getQuantity() - itemRequest.getQuantity());
            branchStockRepository.save(branchStock);

                if (branchStock.getQuantity() <= branchStock.getLowStockThreshold()) {
                    System.out.println("LOW STOCK ALERT at " + request.getBranch() + " for " + drink.getDrinkName() + ". Current stock: " + branchStock.getQuantity());
                    RestockRequest restockRequest = new RestockRequest();
                    restockRequest.setBranch(request.getBranch());
                    restockRequest.setDrink(drink);
                    restockRequest.setRequestedQuantity(50); // Default restock quantity
                    restockRequest.setRequestDate(LocalDateTime.now());
                    restockRequestRepository.save(restockRequest);
                }
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalOrderAmount);

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(order.getCustomer() != null ? order.getCustomer().getCustomer_name() : null);
        response.setCustomerPhoneNumber(order.getCustomer() != null ? order.getCustomer().getCustomerPhoneNumber() : null);
        response.setOrderDate(order.getOrderDate());
        response.setBranch(order.getBranch());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getDrink().getDrinkName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();
        response.setItems(itemResponses);
        return response;
    }

    private String generateOrderNumber() {
        long timestampPart = System.currentTimeMillis() % 1000000;
        int randomPart = (int) (Math.random() * 1000);
        return "ORD-" + timestampPart + "-" + randomPart;
    }

    @Override
    public double calculateTotal(long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return order.getItems().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    @Override
    @Transactional
    public void changeOrderStatusAndUpdateInventory(int orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderId((long)orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot change status of an order that is not PENDING.");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem orderItem : order.getItems()) {
                Drink drink = orderItem.getDrink();
                BranchStock branchStock = branchStockRepository.findByBranchAndDrink(order.getBranch(), drink)
                        .orElseThrow(() -> new RuntimeException("Error finding stock for cancelled order"));
                branchStock.setQuantity(branchStock.getQuantity() + orderItem.getQuantity());
                branchStockRepository.save(branchStock);
            }
        }
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }
}