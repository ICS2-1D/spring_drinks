package com.ics.dto;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    // Customer Details (for Customers table / embedded in Orders table)
    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name cannot exceed 255 characters")
    private String customerName; // Maps to orders.customer_name

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    private String customerPhoneNumber; // Maps to orders.customer_phone_number and payments.customer_number

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String customerEmail; // Not directly mapped to your current DB schema, but kept for data capture

    // Delivery Address (assuming a single 'delivery_address' field in orders table for simplicity)
    @NotBlank(message = "Delivery address is required")
    @Size(max = 500, message = "Delivery address cannot exceed 500 characters")
    private String deliveryAddress; // Maps to orders.delivery_address (combined if multiple fields exist in frontend)

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String deliveryCity; // Will be combined into deliveryAddress string, or mapped to new column if added

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String deliveryPostalCode; // Will be combined into deliveryAddress string, or mapped to new column if added

    @NotNull(message = "Branch is required")
    // Consider creating a Branch enum in your backend (e.g., Branch.MAIN_BRANCH)
    private String branch; // Maps to orders.branch

    // Payment Details
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "M-PESA|CARD|CASH_ON_DELIVERY", message = "Invalid payment method")
    private String paymentMethod; // Maps to payments.payment_method

    // --- IMPORTANT: These fields should NOT be stored in your DB for PCI DSS compliance ---
    // They are only for receiving data from frontend to pass to a payment gateway.
    private String cardNumber; // To be sent to payment gateway, not stored
    private String expiry;     // To be sent to payment gateway, not stored
    private String cvv;        // To be sent to payment gateway, not stored
    // --- END IMPORTANT ---

    // Special Instructions
    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    private String specialInstructions; // New column in orders table if you want to store this

    // Order Items
    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemDTO> orderItems; // Represents items to be saved in order_items table

    // Total amount (for orders.total_amount)
    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount cannot be negative")
    private Double totalAmount; // Maps to orders.total_amount (should be re-calculated on backend for accuracy/security)
}