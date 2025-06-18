package com.ics.controller;

import com.ics.dto.OrderRequestDTO;
import com.ics.model.Order;
import com.ics.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:8080") // Adjust this to your frontend URL(s)
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<String> placeOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        try {
            Order placedOrder = orderService.placeOrder(orderRequestDTO);
            return new ResponseEntity<>("Order placed successfully! Order ID: " + placedOrder.getOrderId(), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle specific validation/business logic errors
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {
            // Catch other unexpected errors (e.g., database issues)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error placing order: " + e.getMessage(), e);
        }
    }
}