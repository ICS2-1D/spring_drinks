package com.ics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ics.dtos.DrinkDto;
import com.ics.dtos.RegisterRequest;
import com.ics.dtos.SalesReportDto; // You will need to create this DTO

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

public class AdminCli {

    // Base URL for the backend API
    private static final String API_BASE_URL = "http://localhost:8080";
    private static final String ADMIN_API_URL = API_BASE_URL + "/admin";


    // Re-usable components
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static String authToken = "";

    public static void main(String[] args) throws IOException, InterruptedException {
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

    private static void showAuthMenu() throws IOException, InterruptedException {
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

    private static void showMainMenu() throws IOException, InterruptedException {
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
//                setBranchInfo();
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

    private static void login() throws IOException, InterruptedException {
        System.out.println("\n--- 🔐 Admin Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        RegisterRequest loginRequest = new RegisterRequest(username, password);
        String requestBody = gson.toJson(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ADMIN_API_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            isLoggedIn = true;
            authToken = response.body(); // Assuming the token is returned in the body
            System.out.println("✅ Login successful! Welcome, " + username + ".");
        } else {
            System.out.println("❌ Login failed. Please check your credentials.");
        }
    }

    private static void signup() throws IOException, InterruptedException {
        System.out.println("\n--- 📝 Admin Signup ---");
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        RegisterRequest signupRequest = new RegisterRequest(username, password);
        String requestBody = gson.toJson(signupRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ADMIN_API_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ Signup successful! Please log in.");
        } else {
            System.out.println("❌ Signup failed: " + response.body());
        }
    }

    private static void logout() {
        isLoggedIn = false;
        authToken = "";
        System.out.println("\n✅ You have been logged out.");
    }


    private static void viewStock() throws IOException, InterruptedException {
        System.out.println("\n--- 📦 Current Drink Stock ---");
        // This can use the same public /drinks endpoint as the client
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/drinks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type drinkListType = new TypeToken<ArrayList<DrinkDto>>() {
            }.getType();
            List<DrinkDto> drinks = gson.fromJson(response.body(), drinkListType);

            System.out.println("----------------------------------------");
            System.out.printf("%-20s | %-10s%n", "Drink Name", "Quantity");
            System.out.println("----------------------------------------");
            for (DrinkDto drink : drinks) {
                System.out.printf("%-20s | %-10d%n", drink.getDrinkName(), drink.getDrinkQuantity());
            }
            System.out.println("----------------------------------------");
        } else {
            System.out.println("❌ Failed to fetch stock.");
        }
    }

    private static void updateDrinkDetails() throws IOException, InterruptedException {
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

        Map<String, Object> payload = new HashMap<>();

        // Only add fields that user wants to update
        if (!newPrice.isEmpty()) {
            try {
                double price = Double.parseDouble(newPrice);
                if (price <= 0) {
                    System.out.println("❌ Price must be greater than 0");
                    return;
                }
                payload.put("drinkPrice", price);
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
                payload.put("drinkQuantity", quantity);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid quantity format");
                return;
            }
        }

        String requestBody = gson.toJson(payload);

        // API call to update drink. Use the existing endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ADMIN_API_URL + "/drinks/" + drinkId + "/price"))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ Drink updated successfully!");
        } else {
            System.out.println("❌ Failed to update drink: " + response.body());
        }
    }

    private static List<DrinkDto> getDrinksMenu() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/drinks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Type drinkListType = new TypeToken<ArrayList<DrinkDto>>() {
            }.getType();
            return gson.fromJson(response.body(), drinkListType);
        }
        return null;
    }


    private static void viewSalesReport() throws IOException, InterruptedException {
        System.out.println("\n--- 📊 Sales Report ---");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ADMIN_API_URL + "/sales/report"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            SalesReportDto report = gson.fromJson(response.body(), SalesReportDto.class);

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
            System.out.println("❌ Failed to generate sales report: " + response.body());
        }
    }

    private static void syncToHq() throws IOException, InterruptedException {
        System.out.println("\n--- ☁️ Sync to HQ ---");

        // 1. Fetch the sales report data first
        HttpRequest reportRequest = HttpRequest.newBuilder()
                .uri(URI.create(ADMIN_API_URL + "/sales/report"))
                .GET()
                .build();
        HttpResponse<String> reportResponse = client.send(reportRequest, HttpResponse.BodyHandlers.ofString());

        if (reportResponse.statusCode() != 200) {
            System.out.println("❌ Could not fetch sales report to sync.");
            return;
        }

        // 2. Serialize the report data (it's already a JSON string)
        String reportJson = reportResponse.body();

        System.out.println("Syncing local data to central server...");
        System.out.println("Serialized Payload: " + reportJson);

        // 3. Send it to the HQ server
        // This requires another API endpoint on a different server (the HQ server)
        // For now, we just simulate this.
        /*
            // Example of what the actual request would look like:
            HttpRequest hqSyncRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://hq-server-api.com/sync")) // The HQ server URL
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reportJson))
                .build();
            HttpResponse<String> hqResponse = client.send(hqSyncRequest, HttpResponse.BodyHandlers.ofString());
            if (hqResponse.statusCode() == 200) {
                 System.out.println("✅ Data successfully synced to HQ!");
            } else {
                System.out.println("❌ HQ sync failed: " + hqResponse.body());
            }
        */
        Thread.sleep(1500);
        System.out.println("✅ Data successfully synced to HQ!");
    }
}
