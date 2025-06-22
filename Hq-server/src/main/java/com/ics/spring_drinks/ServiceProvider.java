package com.ics.spring_drinks;

import com.ics.spring_drinks.services.*;


public record ServiceProvider(
        DrinkService drinkService,
        OrderService orderService,
        AdminService adminService,
        PaymentService paymentService,
        ReportService reportService
) {}