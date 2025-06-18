package com.ics;

import com.ics.dtos.DrinkDto;
import com.ics.dtos.RegisterRequest;
import com.ics.dtos.SalesReportDto;
import com.ics.dtos.Request;
import com.ics.dtos.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminCli {

    // Socket client for communication with HQ server
    private static final SocketClient socketClient = new SocketClient("localhost", 9999);

    // Request type constants
    private static final String LOGIN_ADMIN = "LOGIN_ADMIN";
    private static final String REGISTER_ADMIN = "REGISTER_ADMIN";
    private static final String GET_ALL_DRINKS = "GET_ALL_DRINKS";
    private static final String UPDATE_DRINK = "UPDATE_DRINK";
    private static final String GET_SALES_REPORT = "GET_SALES_REPORT";
    private static final String SYNC_TO_HQ = "SYNC_TO_HQ";

    // Re-usable components
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static String authToken = "";

    public static void main(String[] args) {
        System.out.println("🍹====================================🍹");
        System.out.println("  ADMINISTRATOR COMMAND LINE INTERFACE");
        System.out.println("🍹====================================🍹");

        while (true) {
            if (!isLoggedIn) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n--- Authentication ---");
        System.out.println("1. 🔐 Login");
        System.out.println("2. 📝 Signup");
        System.out.println("0. 🚪 Exit");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                signup();
                break;
            case "0":
                System.out.println("\nExiting. Goodbye! 👋");
                System.exit(0);
                break;
            default:
                System.out.println("❌ Invalid choice. Please try again.");
                break;
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. 📦 View Stock");
        System.out.println("2. 💰 Update Drink Prices");
        System.out.println("3. 🏪 Set Branch Info");
        System.out.println("4. 📊 View Sales Report");
        System.out.println("5. ☁️ Sync to HQ");
        System.out.println("6. 🚪 Logout");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                viewStock();
                break;
            case "2":
                updateDrinkDetails();
                break;
            case "3":
                // setBranchInfo(); // TODO: Implement later
                System.out.println("🚧 Feature coming soon!");
                break;
            case "4":
                viewSalesReport();
                break;
            case "5":
                syncToHq();
                break;
            case "6":
                logout();
                break;
            default:
                System.out.println("❌ Invalid choice. Please try again.");
        }
    }

    private static void login() {
        System.out.println("\n--- 🔐 Admin Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        RegisterRequest loginRequest = new RegisterRequest(username, password);
        Request serviceRequest = new Request(LOGIN_ADMIN, loginRequest);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            isLoggedIn = true;
            authToken = (String) serviceResponse.getData(); // Assuming token is returned in data
            System.out.println("✅ Login successful! Welcome, " + username + ".");
        } else {
            System.out.println("❌ Login failed: " + serviceResponse.getMessage());
        }
    }

    private static void signup() {
        System.out.println("\n--- 📝 Admin Signup ---");
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        RegisterRequest signupRequest = new RegisterRequest(username, password);
        Request serviceRequest = new Request(REGISTER_ADMIN, signupRequest);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            System.out.println("✅ Signup successful! Please log in.");
        } else {
            System.out.println("❌ Signup failed: " + serviceResponse.getMessage());
        }
    }

    private static void logout() {
        isLoggedIn = false;
        authToken = "";
        System.out.println("\n✅ You have been logged out.");
    }

    private static void viewStock() {
        System.out.println("\n--- 📦 Current Drink Stock ---");

        Request serviceRequest = new Request(GET_ALL_DRINKS, null);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<DrinkDto> drinks = (List<DrinkDto>) serviceResponse.getData();

            System.out.println("----------------------------------------");
            System.out.printf("%-20s | %-10s%n", "Drink Name", "Quantity");
            System.out.println("----------------------------------------");
            for (DrinkDto drink : drinks) {
                System.out.printf("%-20s | %-10d%n", drink.getDrinkName(), drink.getDrinkQuantity());
            }
            System.out.println("----------------------------------------");
        } else {
            System.out.println("❌ Failed to fetch stock: " + serviceResponse.getMessage());
        }
    }

    private static void updateDrinkDetails() {
        System.out.println("\n--- 💰 Update Drink Prices & Quantities ---");

        // First, fetch and display drinks so admin can choose one
        List<DrinkDto> drinks = getDrinksMenu();
        if (drinks == null || drinks.isEmpty()) {
            System.out.println("No drinks found to update.");
            return;
        }

        System.out.println("--- Available Drinks ---");
        System.out.printf("%-5s %-20s %-10s %-10s%n", "ID", "Name", "Price", "Quantity");
        System.out.println("-----------------------------------------------------");
        for (DrinkDto drink : drinks) {
            System.out.printf("%-5d %-20s $%-9.2f %-10d%n",
                    drink.getId(),
                    drink.getDrinkName(),
                    drink.getDrinkPrice(),
                    drink.getDrinkQuantity());
        }
        System.out.println("-----------------------------------------------------");

        System.out.print("Enter Drink ID to update: ");
        String drinkId = scanner.nextLine();

        System.out.print("Enter new price (or press Enter to keep current): ");
        String newPrice = scanner.nextLine().trim();

        System.out.print("Enter new quantity (or press Enter to keep current): ");
        String newQuantity = scanner.nextLine().trim();

        // Check if user wants to update anything
        if (newPrice.isEmpty() && newQuantity.isEmpty()) {
            System.out.println("No changes specified. Operation cancelled.");
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("drinkId", drinkId);
        updateData.put("authToken", authToken);

        // Only add fields that user wants to update
        if (!newPrice.isEmpty()) {
            try {
                double price = Double.parseDouble(newPrice);
                if (price <= 0) {
                    System.out.println("❌ Price must be greater than 0");
                    return;
                }
                updateData.put("drinkPrice", price);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid price format");
                return;
            }
        }

        if (!newQuantity.isEmpty()) {
            try {
                int quantity = Integer.parseInt(newQuantity);
                if (quantity < 0) {
                    System.out.println("❌ Quantity cannot be negative");
                    return;
                }
                updateData.put("drinkQuantity", quantity);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid quantity format");
                return;
            }
        }

        Request serviceRequest = new Request(UPDATE_DRINK, updateData);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            System.out.println("✅ Drink updated successfully!");
        } else {
            System.out.println("❌ Failed to update drink: " + serviceResponse.getMessage());
        }
    }

    private static List<DrinkDto> getDrinksMenu() {
        Request serviceRequest = new Request(GET_ALL_DRINKS, null);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<DrinkDto> drinks = (List<DrinkDto>) serviceResponse.getData();
            return drinks;
        }
        return null;
    }

    private static void viewSalesReport() {
        System.out.println("\n--- 📊 Sales Report ---");

        Map<String, Object> reportRequest = new HashMap<>();
        reportRequest.put("authToken", authToken);

        Request serviceRequest = new Request(GET_SALES_REPORT, reportRequest);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            SalesReportDto report = (SalesReportDto) serviceResponse.getData();

            System.out.println("=================================================");
            System.out.println("           SALES REPORT - " + java.time.LocalDate.now());
            System.out.println("=================================================");
            System.out.printf("💰 Total Sales: $%.2f%n%n", report.getTotalSales());

            if (report.getDrinksSold() != null && !report.getDrinksSold().isEmpty()) {
                System.out.println("📋 Drinks Sold:");
                System.out.printf("%-25s | %-10s | %-12s%n", "Drink Name", "Quantity", "Total Sales");
                System.out.println("-------------------------------------------------");

                // Sort by total sales (descending) for better readability
                report.getDrinksSold().entrySet().stream()
                        .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalPrice(), e1.getValue().getTotalPrice()))
                        .forEach(entry -> {
                            String drinkName = entry.getKey();
                            SalesReportDto.DrinkSale sale = entry.getValue();
                            System.out.printf("%-25s | %-10d | $%-11.2f%n",
                                    drinkName,
                                    sale.getQuantity(),
                                    sale.getTotalPrice());
                        });
            } else {
                System.out.println("📋 No drinks sold yet.");
            }
            System.out.println("=================================================");
        } else {
            System.out.println("❌ Failed to generate sales report: " + serviceResponse.getMessage());
        }
    }

    private static void syncToHq() {
        System.out.println("\n--- ☁️ Sync to HQ ---");
        System.out.println("Syncing local data to central server...");

        Map<String, Object> syncData = new HashMap<>();
        syncData.put("authToken", authToken);
        syncData.put("branchId", "NAIROBI"); // Could be made configurable

        Request serviceRequest = new Request(SYNC_TO_HQ, syncData);
        Response serviceResponse = socketClient.sendRequest(serviceRequest);

        if (serviceResponse.getStatus() == Response.Status.SUCCESS) {
            System.out.println("✅ Data successfully synced to HQ!");
        } else {
            System.out.println("❌ HQ sync failed: " + serviceResponse.getMessage());
        }
    }
}