package com.ics;

import com.ics.dtos.DrinkDto;
import com.ics.dtos.OrderItemRequest;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.dtos.Request;
import com.ics.dtos.Response;
import com.ics.models.Branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientCli {
      private static SocketClient client;
      private static Branch branch;
      private static final Scanner scanner = new Scanner(System.in);

      public static void main(String[] args) {
            System.out.println("=== WELCOME TO SPRING DRINKS ===");

            // Connect to server
            try {
                  client = new SocketClient();
                  client.connect();

                  // Get branch assignment
                  Response response = client.sendRequest(new Request("CONNECT", null));
                  if (response.getStatus() == Response.Status.SUCCESS) {
                        branch = (Branch) response.getData();
                        System.out.println("Connected to " + branch.name() + " branch");
                  } else {
                        System.out.println("Connection failed: " + response.getMessage());
                        return;
                  }
            } catch (Exception e) {
                  System.out.println("Could not connect to server: " + e.getMessage());
                  return;
            }

            // Main menu loop
            runMainMenu();

            // Clean up
            client.disconnect();
      }

      private static void runMainMenu() {
            // Get customer info
            System.out.print("Enter your name: ");
            String customerName = scanner.nextLine();
            System.out.print("Enter your phone: ");
            String customerPhone = scanner.nextLine();

            System.out.println("\nHello " + customerName + "!");

            while (true) {
                  System.out.println("\n=== MAIN MENU ===");
                  System.out.println("1. Order Drinks");
                  System.out.println("0. Exit");
                  System.out.print("Choose option: ");

                  String choice = scanner.nextLine();

                  if (choice.equals("1")) {
                        orderDrinks(customerName, customerPhone);
                  } else if (choice.equals("0")) {
                        break;
                  } else {
                        System.out.println("Invalid choice, try again");
                  }
            }

            System.out.println("Thank you for visiting!");
      }

      private static void orderDrinks(String customerName, String customerPhone) {
            // Get available drinks
            List<DrinkDto> drinks = getDrinks();
            if (drinks == null || drinks.isEmpty()) {
                  System.out.println("No drinks available right now");
                  return;
            }

            // Show menu
            System.out.println("\n=== DRINKS MENU ===");
            for (int i = 0; i < drinks.size(); i++) {
                  DrinkDto drink = drinks.get(i);
                  System.out.println((i + 1) + ". " + drink.getDrinkName() + " - $" + drink.getDrinkPrice());
            }

            // Let customer pick drinks
            List<OrderItemRequest> orderItems = new ArrayList<>();

            while (true) {
                  System.out.print("\nPick a drink number (or 0 to finish): ");
                  try {
                        int choice = Integer.parseInt(scanner.nextLine());

                        if (choice == 0) {
                              break;
                        }

                        if (choice < 1 || choice > drinks.size()) {
                              System.out.println("Invalid drink number");
                              continue;
                        }

                        DrinkDto selectedDrink = drinks.get(choice - 1);

                        System.out.print("How many " + selectedDrink.getDrinkName() + "? ");
                        int quantity = Integer.parseInt(scanner.nextLine());

                        if (quantity <= 0) {
                              System.out.println("Quantity must be positive");
                              continue;
                        }

                        if (quantity > selectedDrink.getDrinkQuantity()) {
                              System.out.println("Not enough stock! Only " + selectedDrink.getDrinkQuantity() + " available");
                              continue;
                        }

                        orderItems.add(new OrderItemRequest(selectedDrink.getId(), quantity));
                        System.out.println("Added to order!");

                  } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number");
                  }
            }

            if (orderItems.isEmpty()) {
                  System.out.println("No items in order");
                  return;
            }

            // Show order summary and confirm
            showOrderSummary(orderItems, drinks);

            System.out.print("\nConfirm order? (yes/no): ");
            if (!scanner.nextLine().equalsIgnoreCase("yes")) {
                  System.out.println("Order cancelled");
                  return;
            }

            // Place the order
            placeOrder(customerName, customerPhone, orderItems);
      }

      private static List<DrinkDto> getDrinks() {
            Request request = new Request("GET_ALL_DRINKS", null);
            Response response = client.sendRequest(request);

            if (response.getStatus() == Response.Status.SUCCESS) {
                  return (List<DrinkDto>) response.getData();
            } else {
                  System.out.println("Failed to get drinks: " + response.getMessage());
                  return null;
            }
      }

      private static void showOrderSummary(List<OrderItemRequest> orderItems, List<DrinkDto> drinks) {
            System.out.println("\n=== ORDER SUMMARY ===");
            double total = 0;

            for (OrderItemRequest item : orderItems) {
                  // Find the drink details
                  DrinkDto drink = drinks.stream()
                          .filter(d -> d.getId().equals(item.getDrinkId()))
                          .findFirst()
                          .orElse(null);

                  if (drink != null) {
                        double subtotal = drink.getDrinkPrice() * item.getQuantity();
                        System.out.println(drink.getDrinkName() + " x" + item.getQuantity() + " = $" + subtotal);
                        total += subtotal;
                  }
            }

            System.out.println("TOTAL: $" + total);
      }

      private static void placeOrder(String customerName, String customerPhone, List<OrderItemRequest> orderItems) {
            // Create order request
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setCustomerName(customerName);
            orderRequest.setCustomerPhoneNumber(customerPhone);
            orderRequest.setBranch(branch);
            orderRequest.setItems(orderItems);

            // Send order to server
            Request request = new Request("CREATE_ORDER", orderRequest);
            Response response = client.sendRequest(request);

            if (response.getStatus() == Response.Status.SUCCESS) {
                  OrderResponse orderResponse = (OrderResponse) response.getData();
                  System.out.println("\nORDER PLACED! Order #" + orderResponse.getOrderNumber());

                  // Simulate payment
                  System.out.println("Processing payment...");
                  try {
                        Thread.sleep(2000); // Wait 2 seconds
                        System.out.println("Payment successful!");
                  } catch (InterruptedException e) {
                        // Handle interruption
                  }
            } else {
                  System.out.println("Order failed: " + response.getMessage());
            }
      }
}