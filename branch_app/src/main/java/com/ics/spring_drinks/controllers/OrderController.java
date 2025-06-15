package com.ics.spring_drinks.controllers;


import com.ics.dtos.CustomerDto;
import com.ics.dtos.OrderItemResponse;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.models.Customer;
import com.ics.models.Order;
import com.ics.models.OrderStatus;
import com.ics.spring_drinks.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping()
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

    @GetMapping("total/{orderId}")
    public ResponseEntity<?> getTotal(@PathVariable("orderId") String orderId) {
        double total = orderService.calculateTotal(Long.parseLong(orderId));
        return ResponseEntity.ok(total);
    }

    @PutMapping("/status/{orderId}")
    public ResponseEntity<?> updateStatus(@PathVariable("orderId") int orderId, @RequestParam OrderStatus orderStatus) {
        try {
            orderService.changeOrderStatusAndUpdateInventory(orderId, orderStatus);
            return ResponseEntity.ok("Order status updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the order status.");
        }
    }

}
