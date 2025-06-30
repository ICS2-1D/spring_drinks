package com.ics.spring_drinks.services.impl;

import com.ics.dtos.PaymentRequest;
import com.ics.dtos.PaymentResponse;
import com.ics.models.Order;
import com.ics.models.OrderStatus; // IMPORTANT: Import OrderStatus enum
import com.ics.models.Payment;
import com.ics.spring_drinks.repository.OrderRepository;
import com.ics.spring_drinks.repository.PaymentRepository;
import com.ics.spring_drinks.services.OrderService; // IMPORTANT: Import OrderService
import com.ics.spring_drinks.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORTANT: Import for @Transactional

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService; // NEW: Inject OrderService here

    @Override
    @Transactional // NEW: Add @Transactional to ensure atomicity of payment and order status update
    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId())); // Added specific message

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCustomerNumber(request.getCustomerNumber());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(request.getPaymentStatus()); // This takes the status from the frontend request (e.g., 'SUCCESS')
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTime(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // NEW LOGIC: Update order status if payment was successful
        if ("SUCCESS".equalsIgnoreCase(request.getPaymentStatus())) {
            // Call OrderService to change the status of the related order to COMPLETED
            // Ensure orderId is cast to int if changeOrderStatusAndUpdateInventory expects int
            orderService.changeOrderStatusAndUpdateInventory(request.getOrderId().intValue(), OrderStatus.COMPLETED);
        }
        // If the payment fails, you might want to update the order status to FAILED as well
        // else if ("FAILED".equalsIgnoreCase(request.getPaymentStatus())) {
        //     orderService.changeOrderStatusAndUpdateInventory(request.getOrderId().intValue(), OrderStatus.FAILED);
        // }


        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(saved.getPaymentId());
        response.setOrderId(saved.getOrder().getOrderId());
        response.setCustomerNumber(saved.getCustomerNumber());
        response.setPaymentMethod(saved.getPaymentMethod());
        response.setPaymentStatus(saved.getPaymentStatus());
        response.setTransactionId(saved.getTransactionId());
        response.setPaymentTime(saved.getPaymentTime());

        return response;
    }
}
