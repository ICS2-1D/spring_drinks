package com.ics;

import com.ics.dtos.DrinkDto;
import com.ics.dtos.OrderItemRequest;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.dtos.Request;
import com.ics.dtos.Response;
import com.ics.models.Branch;
import com.ics.models.Customer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientCli {
    private static final SocketClient socketClient = new SocketClient("localhost", 9999);
    private static final String GET_ALL_DRINKS = "GET_ALL_DRINKS";
    private static final String CREATE_ORDER = "CREATE_ORDER";
    private static final String UPDATE_ORDER_STATUS = "UPDATE_ORDER_STATUS";
    private static final String CREATE_PAYMENT = "CREATE_PAYMENT";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("      WELCOME TO SPRING DRINKS!");
        System.out.print("Are you an admin? (yes/no): ");
        String adminChoice = scanner.nextLine();

        if ("yes".equalsIgnoreCase(adminChoice)) {
            AdminCli.main(args);
        } else {
            runClientCli();
        }
    }

    private static void runClientCli() {
        Customer customer = welcomeCustomer();
        System.out.println("\n Welcome, " + customer.getCustomer_name());
        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. 🍹 Browse Drinks & Place Order");
            System.out.println("0. 🚪 Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            if ("1".equals(choice)) {
                placeOrder(customer);
            } else if ("0".equals(choice)) {
                break;
            } else {
                System.out.println("❌ Invalid choice. Please try again.");
            }
        }
        System.out.println("\nThank you for visiting! Have a great day! 🎉");
    }

    private static Customer welcomeCustomer() {
        System.out.print("👋 What's your name? ");
        String name = scanner.nextLine();
        System.out.print("📱 What's your phone number? ");
        String phone = scanner.nextLine();

        Customer customer = new Customer();
        customer.setCustomer_name(name);
        customer.setCustomerPhoneNumber(phone);
        return customer;
    }

    private static void placeOrder(Customer customer) {
        List<DrinkDto> availableDrinks = getDrinksMenu();
        if (availableDrinks == null || availableDrinks.isEmpty()) {
            System.out.println("Sorry, no drinks are available at the moment.");
            return;
        }
        System.out.println("\n--- 🍹 DRINKS MENU 🍹 ---");
        System.out.printf("%-5s %-20s %-10s%n", "ID", "Drink Name", "Price");
        System.out.println("---------------------------------------");
        Map<Long, DrinkDto> drinksMap = new HashMap<>();
        for (DrinkDto drink : availableDrinks) {
            System.out.printf("%-5d %-20s Ksh %-9.2f%n", drink.getId(), drink.getDrinkName(), drink.getDrinkPrice());
            drinksMap.put(drink.getId(), drink);
        }
        System.out.println("---------------------------------------");

        Map<Long, OrderItemRequest> cart = new HashMap<>();
        while (true) {
            System.out.print("\nEnter Drink ID to add to cart (or type 'done' to checkout): ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("done")) {
                break;
            }

            try {
                long drinkId = Long.parseLong(input);
                if (!drinksMap.containsKey(drinkId)) {
                    System.out.println("❌ Invalid Drink ID.");
                    continue;
                }

                DrinkDto selectedDrink = drinksMap.get(drinkId);
                System.out.print("How many '" + selectedDrink.getDrinkName() + "' would you like? ");
                int quantity = Integer.parseInt(scanner.nextLine());

                if (quantity <= 0) {
                    System.out.println("❌ Quantity must be positive.");
                } else {
                    OrderItemRequest item = new OrderItemRequest(drinkId, quantity);
                    cart.put(drinkId, item);
                    System.out.println("✅ Added to cart!");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
            }
        }

        if (cart.isEmpty()) {
            System.out.println("Cart is empty. Returning to main menu.");
            return;
        }
        checkout(customer, new ArrayList<>(cart.values()), drinksMap);
    }

    private static List<DrinkDto> getDrinksMenu() {
        Request serviceRequest = new Request(GET_ALL_DRINKS, null);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<DrinkDto> drinks = (List<DrinkDto>) serviceResponse.getData();
            return drinks;
        } else {
            System.out.println("❌ Failed to fetch drinks menu: " + serviceResponse.getMessage());
            return null;
        }
    }

    private static void checkout(Customer customer, List<OrderItemRequest> items, Map<Long, DrinkDto> drinksMap) {
        System.out.println("\n--- 🛒 YOUR ORDER SUMMARY ---");
        double total = 0;
        System.out.printf("%-20s %-10s %-10s %-10s%n", "Drink", "Quantity", "Unit Price", "Subtotal");
        System.out.println("---------------------------------------------------");
        for (OrderItemRequest item : items) {
            DrinkDto drink = drinksMap.get(item.getDrinkId());
            double subtotal = drink.getDrinkPrice() * item.getQuantity();
            System.out.printf("%-20s %-10d Ksh %-9.2f $%-9.2f%n", drink.getDrinkName(), item.getQuantity(), drink.getDrinkPrice(), subtotal);
            total += subtotal;
        }
        System.out.println("---------------------------------------------------");
        System.out.printf("TOTAL: Ksh %.2f%n", total);

        System.out.print("\nType 'YES' to confirm and pay: ");
        String confirm = scanner.nextLine();

        if (!confirm.equalsIgnoreCase("YES")) {
            System.out.println("❌ Order cancelled.");
            return;
        }

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerName(customer.getCustomer_name());
        orderRequest.setCustomerPhoneNumber(customer.getCustomerPhoneNumber());
        orderRequest.setBranch(Branch.NAIROBI);
        orderRequest.setItems(items);

        Request serviceRequest = new Request(CREATE_ORDER, orderRequest);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            OrderResponse orderResponse = (OrderResponse) serviceResponse.getData();
            System.out.println("\n🎉 ORDER PLACED SUCCESSFULLY! Order Number: " + orderResponse.getOrderNumber());
            simulatePayment(orderResponse.getOrderId(), customer.getCustomerPhoneNumber());
        } else {
            System.out.println("❌ Failed to place order: " + serviceResponse.getMessage());
        }
    }

    private static void simulatePayment(Long orderId, String phoneNumber) {
        System.out.println("\n--- 💳 SIMULATING PAYMENT ---");
        try {
            Thread.sleep(1000);
            System.out.println("Processing with phone number...");
            Thread.sleep(1500);
            System.out.println("✅ Payment Approved!");

            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("orderId", orderId);
            statusUpdate.put("orderStatus", "COMPLETED");

            Request statusRequest = new Request(UPDATE_ORDER_STATUS, statusUpdate);
            Response statusResponse = socketClient.sendRequest(statusRequest);

            if (statusResponse.getStatus() != Response.Status.SUCCESS) {
                System.out.println("⚠️ Warning: Could not update order status");
            }

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("orderId", orderId);
            paymentData.put("customerNumber", phoneNumber);
            paymentData.put("paymentMethod", "M-PESA");
            paymentData.put("paymentStatus", "SUCCESS");

            Request paymentRequest = new Request(CREATE_PAYMENT, paymentData);
            Response paymentResponse = socketClient.sendRequest(paymentRequest);

            if (paymentResponse.getStatus() == Response.Status.SUCCESS) {
                System.out.println("✅ Payment recorded successfully!");
            } else {
                System.out.println("❌ Failed to record payment: " + paymentResponse.getMessage());
            }

        } catch (InterruptedException e) {
            System.out.println("Payment simulation interrupted");
            Thread.currentThread().interrupt();
        }
    }
}