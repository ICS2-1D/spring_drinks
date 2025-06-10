package com.ics.distributed_drinks_system.controllers;


import com.ics.distributed_drinks_system.dtos.CustomerDto;
import com.ics.distributed_drinks_system.dtos.OrderItemResponse;
import com.ics.distributed_drinks_system.dtos.OrderRequest;
import com.ics.distributed_drinks_system.dtos.OrderResponse;
import com.ics.distributed_drinks_system.models.Customer;
import com.ics.distributed_drinks_system.models.Order;
import com.ics.distributed_drinks_system.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {

        try{
            Order order = orderService.createOrder(orderRequest);

            OrderResponse orderResponse = convertToOrderResponse(order);


            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the order.");
        }
    }


    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNumber(order.getOrderNumber());
        response.setBranch(order.getBranch());
        response.setOrderStatus(order.getOrderStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());

        Customer customer = order.getCustomer();
        CustomerDto customerDto = new CustomerDto(customer.getId(), customer.getName(), customer.getPhoneNumber());
        response.setCustomerDto(customerDto);


        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getDrink().getDrinkName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                )).collect(Collectors.toList());
        response.setItems(items);
        return response;
    }
}
