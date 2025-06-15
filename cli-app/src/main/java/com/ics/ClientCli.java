package com.ics;

import com.ics.dtos.CustomerDto;
import com.ics.dtos.DrinkDto;
import com.ics.dtos.OrderItemRequest;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.models.Branch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientCli {

   // base URL for the backend API
    private static final String API_BASE_URL = "http://localhost:8080";

    // Re-usable components for making HTTP requests and parsing JSON
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. Welcome the user and register them in the system.
        CustomerDto customer = welcomeAndRegisterCustomer();
        if (customer == null) {
            System.out.println("Could not register customer. Exiting application.");
            return;
        }

        System.out.println("\n✅ Welcome, " + customer.getName() + "! Your customer ID is " + customer.getId());

        while (true) {
            // 2. Main menu loop
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. 🍹 Browse Drinks & Place Order");
            System.out.println("0. 🚪 Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            if ("1".equals(choice)) {
                // 3. Start the ordering process
                placeOrder(customer);
            } else if ("0".equals(choice)) {
                break;
            } else {
                System.out.println("❌ Invalid choice. Please try again.");
            }
        }

        System.out.println("\nThank you for visiting! Have a great day! 🎉");
    }

    private static CustomerDto welcomeAndRegisterCustomer() throws IOException, InterruptedException {
        System.out.println("🍹====================================🍹");
        System.out.println("      WELCOME TO SPRING DRINKS!");
        System.out.println("🍹====================================🍹");
        System.out.print("👋 What's your name? ");
        String name = scanner.nextLine();
        System.out.print("📱 What's your phone number? ");
        String phone = scanner.nextLine();

        // Create a CustomerDto object from user input
        CustomerDto newCustomer = new CustomerDto(null, name, phone);

        // Convert the DTO to a JSON string
        String requestBody = gson.toJson(newCustomer);

        // Build the HTTP POST request to create a customer
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Convert the JSON response back to a CustomerDto object
            return gson.fromJson(response.body(), CustomerDto.class);
        } else {
            System.out.println("Error creating customer: " + response.body());
            return null;
        }
    }

    private static void placeOrder(CustomerDto customer) throws IOException, InterruptedException {
        // Get the list of available drinks from the API
        List<DrinkDto> availableDrinks = getDrinksMenu();
        if (availableDrinks == null || availableDrinks.isEmpty()) {
            System.out.println("Sorry, no drinks are available at the moment.");
            return;
        }

        // Display the menu
        System.out.println("\n--- 🍹 DRINKS MENU 🍹 ---");
        System.out.printf("%-5s %-20s %-10s %-10s%n", "ID", "Drink Name", "Price", "Stock");
        System.out.println("-------------------------------------------------");
        Map<Long, DrinkDto> drinksMap = new HashMap<>();
        for (DrinkDto drink : availableDrinks) {
            System.out.printf("%-5d %-20s $%-9.2f %-10d%n", drink.getId(), drink.getDrinkName(), drink.getDrinkPrice(), drink.getDrinkQuantity());
            drinksMap.put(drink.getId(), drink);
        }
        System.out.println("-------------------------------------------------");


        // Let user add items to their cart
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
                System.out.print("How many '" + selectedDrink.getDrinkName() + "' would you like? (Available: " + selectedDrink.getDrinkQuantity() + "): ");
                int quantity = Integer.parseInt(scanner.nextLine());

                if (quantity <= 0) {
                    System.out.println("❌ Quantity must be positive.");
                } else if (quantity > selectedDrink.getDrinkQuantity()) {
                    System.out.println("❌ Not enough stock.");
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

        // Checkout process
        checkout(customer, new ArrayList<>(cart.values()), drinksMap);
    }


    private static List<DrinkDto> getDrinksMenu() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/drinks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Type drinkListType = new TypeToken<ArrayList<DrinkDto>>(){}.getType();
            return gson.fromJson(response.body(), drinkListType);
        }
        return null;
    }


    private static void checkout(CustomerDto customer, List<OrderItemRequest> items, Map<Long, DrinkDto> drinksMap) throws IOException, InterruptedException {
        System.out.println("\n--- 🛒 YOUR ORDER SUMMARY ---");
        double total = 0;
        System.out.printf("%-20s %-10s %-10s %-10s%n", "Drink", "Quantity", "Unit Price", "Subtotal");
        System.out.println("---------------------------------------------------");
        for(OrderItemRequest item : items) {
            DrinkDto drink = drinksMap.get(item.getDrinkId());
            double subtotal = drink.getDrinkPrice() * item.getQuantity();
            System.out.printf("%-20s %-10d $%-9.2f $%-9.2f%n", drink.getDrinkName(), item.getQuantity(), drink.getDrinkPrice(), subtotal);
            total += subtotal;
        }
        System.out.println("---------------------------------------------------");
        System.out.printf("TOTAL: $%.2f%n", total);

        System.out.print("\nType 'YES' to confirm and pay: ");
        String confirm = scanner.nextLine();

        if (!confirm.equalsIgnoreCase("YES")) {
            System.out.println("❌ Order cancelled.");
            return;
        }

        // NOTE: Assuming Branch ID 1 for this CLI.
        // In a real app, this might be selected or configured.
        Branch branch = Branch.NAIROBI; // ✅ or get from user input
        OrderRequest orderRequest = new OrderRequest(customer.getId(), branch, items);
        String requestBody = gson.toJson(orderRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/order"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) { // 201 Created
            OrderResponse orderResponse = gson.fromJson(response.body(), OrderResponse.class);
            System.out.println("\n🎉 ORDER PLACED SUCCESSFULLY! Order Number: " + orderResponse.getOrderNumber());
            simulatePayment(orderResponse.getOrderId());
        } else {
            System.out.println("❌ Failed to place order: " + response.body());
        }
    }


    private static void simulatePayment(Long orderId) throws IOException, InterruptedException {
        System.out.println("\n--- 💳 SIMULATING PAYMENT ---");
        System.out.println("Processing with phone number...");
        Thread.sleep(1000);
        System.out.println("Contacting payment provider...");
        Thread.sleep(1500);
        System.out.println("✅ Payment Approved!");

        // Update the order status on the backend
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/order/status/" + orderId + "?orderStatus=COMPLETED"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ Order status updated to COMPLETED.");
            System.out.println("Thank you for your purchase!");
        } else {
            System.out.println("Could not update order status on the server: " + response.body());
        }
    }
}