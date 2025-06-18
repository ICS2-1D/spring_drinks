package com.ics.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    @NotBlank(message = "Drink name is required for order item")
    @Size(max = 255, message = "Drink name cannot exceed 255 characters")
    private String drink_name; // To be used to look up drink_id in the 'drinks' table

    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity is required for order item")
    private Integer quantity; // Maps to order_items.quantity

    @Min(value = 0, message = "Unit price cannot be negative")
    @NotNull(message = "Unit price is required for order item")
    private Double unit_price; // Maps to order_items.unit_price

    @NotNull(message = "Total price for item is required")
    private Double total_price; // Maps to order_items.total_price
}

