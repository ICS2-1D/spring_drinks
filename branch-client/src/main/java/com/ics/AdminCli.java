package com.ics;

import com.ics.dtos.DrinkDto;
import com.ics.dtos.RegisterRequest;
import com.ics.dtos.SalesReportDto;
import com.ics.dtos.Request;
import com.ics.dtos.Response;
import com.ics.models.Branch;
import com.ics.models.Customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminCli {
    private static SocketClient client;
    private static Branch branch;
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static String authToken = "";

    public static void main(String[] args) {
        System.out.println("=== ADMIN CONSOLE ===");

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

            // Check if this is Nairobi branch (admin access required)
            if (branch != Branch.NAIROBI) {
                System.out.println("ERROR: Admin access only available from Nairobi branch");
                System.out.println("Current branch: " + branch.name());
                return;
            }

        } catch (Exception e) {
            System.out.println("Could not connect to server: " + e.getMessage());
            return;
        }

        // Main application loop
        runMainMenu();

        // Clean up
        client.disconnect();
    }

    private static void runMainMenu() {
        while (true) {
            if (!isLoggedIn) {
                showLoginMenu();
            } else {
                showAdminMenu();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n=== LOGIN MENU ===");
        System.out.println("1. Login");
        System.out.println("2. Create Account");
        System.out.println("0. Exit");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                createAccount();
                break;
            case "0":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice, try again");
                break;
        }
    }

    private static void showAdminMenu() {
        System.out.println("\n=== ADMIN MENU ===");
        System.out.println("1. View Stock");
        System.out.println("2. Update Drinks");
        System.out.println("3. Sales Report");
        System.out.println("4. Act as Customer");
        System.out.println("0. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                viewStock();
                break;
            case "2":
                updateDrinks();
                break;
            case "3":
                viewSalesReport();
                break;
            case "4":
                actAsCustomer();
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Invalid choice, try again");
                break;
        }
    }

    private static void login() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        RegisterRequest loginRequest = new RegisterRequest(username, password);
        Request request = new Request("LOGIN_ADMIN", loginRequest);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            isLoggedIn = true;
            authToken = (String) response.getData();
            System.out.println("Login successful! Welcome " + username);
        } else {
            System.out.println("Login failed: " + response.getMessage());
        }
    }

    private static void createAccount() {
        System.out.println("\n=== CREATE ACCOUNT ===");
        System.out.print("New username: ");
        String username = scanner.nextLine();
        System.out.print("New password: ");
        String password = scanner.nextLine();

        RegisterRequest signupRequest = new RegisterRequest(username, password);
        Request request = new Request("REGISTER_ADMIN", signupRequest);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            System.out.println("Account created! Please login now");
        } else {
            System.out.println("Account creation failed: " + response.getMessage());
        }
    }

    private static void logout() {
        isLoggedIn = false;
        authToken = "";
        System.out.println("Logged out successfully");
    }

    private static void viewStock() {
        System.out.println("\n=== CURRENT STOCK ===");

        Request request = new Request("GET_ALL_DRINKS", null);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            List<DrinkDto> drinks = (List<DrinkDto>) response.getData();

            System.out.println("Drink Name           | Quantity");
            System.out.println("---------------------|----------");
            for (DrinkDto drink : drinks) {
                System.out.printf("%-20s | %d%n", drink.getDrinkName(), drink.getDrinkQuantity());
            }
        } else {
            System.out.println("Failed to get stock: " + response.getMessage());
        }
    }

    private static void updateDrinks() {
        System.out.println("\n=== UPDATE DRINKS ===");

        // Show current drinks
        List<DrinkDto> drinks = getDrinks();
        if (drinks == null || drinks.isEmpty()) {
            System.out.println("No drinks found");
            return;
        }

        System.out.println("ID | Name                 | Price | Quantity");
        System.out.println("---|----------------------|-------|----------");
        for (DrinkDto drink : drinks) {
            System.out.printf("%-2d | %-20s | $%-4.2f | %d%n",
                    drink.getId(),
                    drink.getDrinkName(),
                    drink.getDrinkPrice(),
                    drink.getDrinkQuantity());
        }

        System.out.print("\nEnter drink ID to update: ");
        String drinkId = scanner.nextLine();

        System.out.print("New price (or press Enter to skip): ");
        String newPrice = scanner.nextLine().trim();

        System.out.print("New quantity (or press Enter to skip): ");
        String newQuantity = scanner.nextLine().trim();

        if (newPrice.isEmpty() && newQuantity.isEmpty()) {
            System.out.println("No changes made");
            return;
        }

        // Prepare update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("drinkId", drinkId);
        updateData.put("authToken", authToken);

        if (!newPrice.isEmpty()) {
            try {
                double price = Double.parseDouble(newPrice);
                if (price <= 0) {
                    System.out.println("Price must be greater than 0");
                    return;
                }
                updateData.put("drinkPrice", price);
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format");
                return;
            }
        }

        if (!newQuantity.isEmpty()) {
            try {
                int quantity = Integer.parseInt(newQuantity);
                if (quantity < 0) {
                    System.out.println("Quantity cannot be negative");
                    return;
                }
                updateData.put("drinkQuantity", quantity);
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity format");
                return;
            }
        }

        Request request = new Request("UPDATE_DRINK", updateData);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            System.out.println("Drink updated successfully!");
        } else {
            System.out.println("Update failed: " + response.getMessage());
        }
    }

    private static List<DrinkDto> getDrinks() {
        Request request = new Request("GET_ALL_DRINKS", null);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            return (List<DrinkDto>) response.getData();
        }
        return null;
    }

    private static void viewSalesReport() {
        System.out.println("\n=== SALES REPORT ===");

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("authToken", authToken);

        Request request = new Request("GET_SALES_REPORT", reportData);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            SalesReportDto report = (SalesReportDto) response.getData();

            System.out.println("Date: " + java.time.LocalDate.now());
            System.out.println("Total Sales: $" + String.format("%.2f", report.getTotalSales()));
            System.out.println();

            if (report.getDrinksSold() != null && !report.getDrinksSold().isEmpty()) {
                System.out.println("Drinks Sold:");
                System.out.println("Drink Name           | Quantity | Total Sales");
                System.out.println("---------------------|----------|------------");

                report.getDrinksSold().entrySet().stream()
                        .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalPrice(), e1.getValue().getTotalPrice()))
                        .forEach(entry -> {
                            String drinkName = entry.getKey();
                            SalesReportDto.DrinkSale sale = entry.getValue();
                            System.out.printf("%-20s | %-8d | $%.2f%n",
                                    drinkName,
                                    sale.getQuantity(),
                                    sale.getTotalPrice());
                        });
            } else {
                System.out.println("No sales yet");
            }
        } else {
            System.out.println("Failed to get sales report: " + response.getMessage());
        }
    }

    private static void actAsCustomer() {
        System.out.println("\n=== CUSTOMER MODE ===");
        System.out.println("You are now acting as a customer");

        // Create a customer object for the admin
        Customer adminCustomer = new Customer();
        adminCustomer.setCustomer_name("Admin Customer");
        adminCustomer.setCustomer_phone_number("0700000000");

        System.out.println("Placing order from " + branch.name() + " branch");

        // Use the simplified order method from ClientCli
        placeOrder(adminCustomer);
    }

    // Simplified version of the order placement method
    public static void placeOrder(Customer customer) {
        // Get available drinks
        List<DrinkDto> drinks = getDrinks();
        if (drinks == null || drinks.isEmpty()) {
            System.out.println("No drinks available");
            return;
        }

        // Show menu (simplified version)
        System.out.println("\n=== DRINKS MENU ===");
        for (int i = 0; i < drinks.size(); i++) {
            DrinkDto drink = drinks.get(i);
            System.out.println((i + 1) + ". " + drink.getDrinkName() + " - $" + drink.getDrinkPrice());
        }

        System.out.print("\nPick a drink number (1-" + drinks.size() + "): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice < 1 || choice > drinks.size()) {
                System.out.println("Invalid choice");
                return;
            }

            DrinkDto selectedDrink = drinks.get(choice - 1);

            System.out.print("How many? ");
            int quantity = Integer.parseInt(scanner.nextLine());

            if (quantity <= 0) {
                System.out.println("Quantity must be positive");
                return;
            }

            if (quantity > selectedDrink.getDrinkQuantity()) {
                System.out.println("Not enough stock! Only " + selectedDrink.getDrinkQuantity() + " available");
                return;
            }

            double total = selectedDrink.getDrinkPrice() * quantity;
            System.out.println("\nOrder: " + selectedDrink.getDrinkName() + " x" + quantity + " = $" + total);
            System.out.print("Confirm order? (yes/no): ");

            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                System.out.println("Order placed successfully!");
                System.out.println("Processing payment...");
                try {
                    Thread.sleep(2000);
                    System.out.println("Payment successful!");
                } catch (InterruptedException e) {
                    // Handle interruption
                }
            } else {
                System.out.println("Order cancelled");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number");
        }
    }
}