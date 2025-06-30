package com.ics.spring_drinks.services.impl;

import com.ics.dtos.OrderItemResponse;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.models.*;
import com.ics.spring_drinks.repository.CustomerRepository; // NEW: Import CustomerRepository
import com.ics.spring_drinks.repository.DrinkRepository;
import com.ics.spring_drinks.repository.OrderItemRepository; // Still keeping this if needed for other operations, but not for saveAll here
import com.ics.spring_drinks.repository.OrderRepository;
import com.ics.spring_drinks.services.OrderService;
import jakarta.transaction.Transactional; // Use jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime; // Use LocalDateTime for current timestamp
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DrinkRepository drinkRepository;
    private final OrderItemRepository orderItemRepository; // Keep this, but saveAll will be removed from createOrder
    private final CustomerRepository customerRepository; // NEW: Inject CustomerRepository


    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Handle Customer (find existing or create new)
        Customer customer = customerRepository.findByCustomerPhoneNumber(request.getCustomerPhoneNumber())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomer_name(request.getCustomerName());
                    newCustomer.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
                    // It's good practice to save the new customer explicitly if not relying purely on order cascade PERSIST
                    return customerRepository.save(newCustomer);
                });


        // 2. Create Order entity and set initial properties
        Order order = new Order();
        order.setCustomer(customer); // Link the customer
        order.setOrderNumber(generateOrderNumber());
        order.setBranch(request.getBranch());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now())); // Use LocalDateTime for current time

        List<OrderItem> orderItems = new ArrayList<>();
        double totalOrderAmount = 0.0;

        // 3. Process each OrderItemRequest
        for (var itemRequest : request.getItems()) {
            Drink drink = drinkRepository.findById(itemRequest.getDrinkId())
                    .orElseThrow(() -> new RuntimeException("Drink with ID " + itemRequest.getDrinkId() + " not found."));

            if (drink.getDrinkQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for drink: " + drink.getDrinkName()
                        + ". Available: " + drink.getDrinkQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order); // Associate OrderItem with the Order (JPA will handle persistence)
            orderItem.setDrink(drink);
            orderItem.setQuantity(itemRequest.getQuantity()); // Ensure quantity is set from request
            orderItem.setUnitPrice(drink.getDrinkPrice()); // Get current drink price as unit price
            orderItem.setTotalPrice(drink.getDrinkPrice() * itemRequest.getQuantity()); // Calculate total price

            orderItems.add(orderItem);
            totalOrderAmount += orderItem.getTotalPrice();

            // 4. Update Drink Stock in Inventory
            drink.setDrinkQuantity(drink.getDrinkQuantity() - itemRequest.getQuantity());
            drinkRepository.save(drink); // Save the updated drink
        }

        // 5. Set order items and total amount on the order
        order.setItems(orderItems); // This sets the collection in the Order entity
        order.setTotalAmount(totalOrderAmount);

        // 6. Save the Order (OrderItem entities will be cascaded and saved automatically)
        Order savedOrder = orderRepository.save(order);

        // 7. Map to OrderResponse DTO
        return mapToOrderResponse(savedOrder);
    }


    // --- Existing methods (no changes needed here, just including for context) ---

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(order.getCustomer() != null ? order.getCustomer().getCustomer_name() : null); // Handle null customer for safety
        response.setCustomerPhoneNumber(order.getCustomer() != null ? order.getCustomer().getCustomerPhoneNumber() : null); // Handle null customer for safety
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
        response.setItems(itemResponses); // Set the items in the response
        return response;

    }


    private String generateOrderNumber() {
        long timestampPart = System.currentTimeMillis() % 1000000; // reduce length a bit
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
        Order order = orderRepository.findByOrderId((long)orderId) // Cast to long as OrderRepository uses Long
                .orElseThrow(() -> new RuntimeException("Order not found"));
        // Only allow status changes from PENDING
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot change status of an order that is not PENDING.");
        }

        if (newStatus == OrderStatus.COMPLETED) {
            // Stock was already decremented at order creation. No need to decrement again.
            // If the business logic changes to decrement stock on COMPLETION, this part needs review.
        }
        else if (newStatus == OrderStatus.CANCELLED) {
            // If order is cancelled, return items to stock
            for (OrderItem orderItem : order.getItems()) {
                Drink drink = orderItem.getDrink();
                drink.setDrinkQuantity(drink.getDrinkQuantity() + orderItem.getQuantity());
                drinkRepository.save(drink);
            }
        }
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }
}