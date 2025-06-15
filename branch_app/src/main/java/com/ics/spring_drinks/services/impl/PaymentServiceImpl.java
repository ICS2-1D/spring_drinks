package com.ics.spring_drinks.services.impl;

import com.ics.dtos.PaymentRequest;
import com.ics.dtos.PaymentResponse;
import com.ics.models.Order;
import com.ics.models.Payment;
import com.ics.spring_drinks.repository.OrderRepository;
import com.ics.spring_drinks.repository.PaymentRepository;
import com.ics.spring_drinks.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCustomerNumber(request.getCustomerNumber());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(request.getPaymentStatus());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTime(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

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

