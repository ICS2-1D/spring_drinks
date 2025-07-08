package com.ics;

import com.ics.dtos.*;
import com.ics.models.Branch;
import com.ics.models.Customer;

import java.util.*;

public class AdminCli {
    private static SocketClient client;
    private static Branch branch;
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static String authToken = "";

    public static void main(String[] args) {
        System.out.println("=== ADMIN CONSOLE ===");
        try {
            client = new SocketClient();
            client.connect();
            Response response = client.sendRequest(new Request("CONNECT_ADMIN", null));
            if (response.getStatus() == Response.Status.SUCCESS) {
                branch = (Branch) response.getData();
                System.out.println("Connected to " + branch.name() + " branch");
            } else {
                System.out.println("Connection failed: " + response.getMessage());
                return;
            }
            if (branch != Branch.NAIROBI) {
                System.out.println("ERROR: Admin access only available from Nairobi branch");
                System.out.println("Current branch: " + branch.name());
                return;
            }
        } catch (Exception e) {
            System.out.println("Could not connect to server: " + e.getMessage());
            return;
        }
        runMainMenu();
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
            case "1": login(); break;
            case "2": createAccount(); break;
            case "0": System.out.println("Goodbye!"); System.exit(0); break;
            default: System.out.println("Invalid choice, try again"); break;
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

    private static void showAdminMenu() {
        System.out.println("\n=== ADMIN MENU ===");
        System.out.println("1. View Stock");
        System.out.println("2. Update Drinks");
        System.out.println("3. Sales Report");
        System.out.println("4. View Low Stock");
        System.out.println("5. Add New Drink Product");
        System.out.println("0. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1": viewStock(); break;
            case "2": updateDrinks(); break;
            case "3": viewSalesReport(); break;
            case "4": viewLowStock(); break;
            case "5": addDrink(); break;
            case "0": logout(); break;
            default: System.out.println("Invalid choice, try again"); break;
        }
    }

    private static void addDrink() {
        System.out.println("\n=== ADD NEW DRINK PRODUCT ===");
        try {
            System.out.print("Enter new drink name: ");
            String name = scanner.nextLine();
            System.out.print("Enter price (e.g., 2.50): ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter initial quantity for all branches: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            if (name.trim().isEmpty() || price <= 0 || quantity < 0) {
                System.out.println("Invalid input. Name cannot be empty, price and quantity must be positive.");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("drinkName", name);
            payload.put("drinkPrice", price);
            payload.put("drinkQuantity", quantity);

            Request request = new Request("ADD_DRINK", payload);
            Response response = client.sendRequest(request);

            if (response.getStatus() == Response.Status.SUCCESS) {
                System.out.println("✅ Successfully added '" + name + "' to all branches!");
            } else {
                System.out.println("❌ Failed to add drink: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format for price or quantity.");
        }
    }

    private static void viewLowStock() {
        System.out.println("\n=== LOW STOCK ITEMS (ALL BRANCHES) ===");
        Request request = new Request("GET_LOW_STOCK", null);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            List<DrinkDto> drinks = (List<DrinkDto>) response.getData();
            if (drinks == null || drinks.isEmpty()) {
                System.out.println("No items are currently low on stock.");
                return;
            }
            System.out.println("ID | Drink Name           | Branch    | Quantity");
            System.out.println("---|----------------------|-----------|----------");
            for (DrinkDto drink : drinks) {
                System.out.printf("%-2d | %-20s | %-9s | %d%n", drink.getId(), drink.getDrinkName(), drink.getBranch(), drink.getDrinkQuantity());
            }

            System.out.print("\nDo you want to restock an item? (yes/no): ");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                restockDrinkFromLowStock();
            }

        } else {
            System.out.println("Failed to get low stock items: " + response.getMessage());
        }
    }

    private static void restockDrinkFromLowStock() {
        try {
            System.out.print("Enter the ID of the drink to restock: ");
            long drinkId = Long.parseLong(scanner.nextLine());

            System.out.print("Enter the Branch Name for the restock: ");
            String branchName = scanner.nextLine().toUpperCase();
            try {
                Branch.valueOf(branchName);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid branch name. Please use one of: NAIROBI, MOMBASA, KISUMU, ELDORET");
                return;
            }

            System.out.print("Enter quantity to add: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            if (quantity <= 0) {
                System.out.println("Quantity must be a positive number.");
                return;
            }

            Map<String, Object> restockData = new HashMap<>();
            restockData.put("drinkId", drinkId);
            restockData.put("branch", branchName);
            restockData.put("quantity", quantity);

            Request request = new Request("RESTOCK_DRINK", restockData);
            Response response = client.sendRequest(request);

            if (response.getStatus() == Response.Status.SUCCESS) {
                System.out.println("Restock successful!");
            } else {
                System.out.println("Restock failed: " + response.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter a valid ID and quantity.");
        }
    }


    private static void viewStock() {
        System.out.println("\n=== CURRENT STOCK (" + branch.name() + " Branch) ===");
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
        System.out.println("\n=== UPDATE DRINKS (" + branch.name() + " Branch) ===");
        List<DrinkDto> drinks = getDrinks();
        if (drinks == null || drinks.isEmpty()) {
            System.out.println("No drinks found");
            return;
        }

        System.out.println("ID | Name                 | Price | Quantity");
        System.out.println("---|----------------------|-------|----------");
        for (DrinkDto drink : drinks) {
            System.out.printf("%-2d | %-20s | ksh %-4.2f | %d%n",
                    drink.getId(), drink.getDrinkName(), drink.getDrinkPrice(), drink.getDrinkQuantity());
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

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("drinkId", drinkId);
        updateData.put("authToken", authToken);

        if (!newPrice.isEmpty()) {
            try {
                updateData.put("drinkPrice", Double.parseDouble(newPrice));
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format");
                return;
            }
        }
        if (!newQuantity.isEmpty()) {
            try {
                updateData.put("drinkQuantity", Integer.parseInt(newQuantity));
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
        return (response.getStatus() == Response.Status.SUCCESS) ? (List<DrinkDto>) response.getData() : null;
    }

    private static void viewSalesReport() {
        System.out.println("\n=== SALES REPORT MENU ===");
        System.out.println("1. Consolidated Report (All Branches)");
        System.out.println("2. Report for a Specific Branch");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choose option: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                getConsolidatedReport();
                break;
            case "2":
                getSingleBranchReport();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void getConsolidatedReport() {
        Map<String, Object> reportData = new HashMap<>();

        Request request = new Request("GET_SALES_REPORT", reportData);
        Response response = client.sendRequest(request);

        if (response.getStatus() == Response.Status.SUCCESS) {
            ConsolidatedSalesReportDto consolidatedReport = (ConsolidatedSalesReportDto) response.getData();
            displayConsolidatedReport(consolidatedReport);
        } else {
            System.out.println("Failed to get consolidated sales report: " + response.getMessage());
        }
    }

    private static void getSingleBranchReport() {
        System.out.println("\nSelect a branch to generate a report for:");
        Branch[] branches = Branch.values();
        for (int i = 0; i < branches.length; i++) {
            System.out.println((i + 1) + ". " + branches[i].name());
        }
        System.out.print("Choose option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > branches.length) {
                System.out.println("Invalid choice.");
                return;
            }
            Branch selectedBranch = branches[choice - 1];

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("authToken", authToken);
            reportData.put("branch", selectedBranch.name());

            Request request = new Request("GET_SALES_REPORT", reportData);
            Response response = client.sendRequest(request);

            if (response.getStatus() == Response.Status.SUCCESS) {
                SalesReportDto report = (SalesReportDto) response.getData();
                System.out.println("\n--- SALES REPORT FOR " + selectedBranch.name() + " ---");
                displaySingleReport(report);
            } else {
                System.out.println("Failed to get sales report for " + selectedBranch.name() + ": " + response.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private static void displayConsolidatedReport(ConsolidatedSalesReportDto report) {
        System.out.println("\n=============================================");
        System.out.println("==      CONSOLIDATED SALES REPORT      ==");
        System.out.println("=============================================");
        System.out.println("Date: " + java.time.LocalDate.now());
        System.out.printf("GRAND TOTAL (ALL BRANCHES): ksh %.2f%n", report.getGrandTotalSales());
        System.out.println("---------------------------------------------");

        if(report.getSalesByBranch() == null || report.getSalesByBranch().isEmpty()){
            System.out.println("\nNo sales data available for any branch.");
            return;
        }

        report.getSalesByBranch().forEach((branch, branchReport) -> {
            System.out.println("\n--- BRANCH: " + branch.name() + " ---");
            displaySingleReport(branchReport);
        });
        System.out.println("=============================================");
    }

    private static void displaySingleReport(SalesReportDto report) {
        System.out.printf("Total Branch Sales: ksh %.2f%n", report.getTotalSales());

        if (report.getDrinksSold() != null && !report.getDrinksSold().isEmpty()) {
            System.out.println("\n  Drink Name           | Quantity | Total Sales");
            System.out.println("  ---------------------|----------|------------");

            report.getDrinksSold().entrySet().stream()
                    .sorted(Map.Entry.<String, SalesReportDto.DrinkSale>comparingByValue(Comparator.comparing(SalesReportDto.DrinkSale::getTotalPrice).reversed()))
                    .forEach(entry -> {
                        String drinkName = entry.getKey();
                        SalesReportDto.DrinkSale sale = entry.getValue();
                        System.out.printf("  %-20s | %-8d | ksh %.2f%n",
                                drinkName,
                                sale.getQuantity(),
                                sale.getTotalPrice());
                    });
        } else {
            System.out.println("\n  No sales recorded for this branch.");
        }
    }

    private static void actAsCustomer() {
        System.out.println("\n=== CUSTOMER MODE ===\nPlacing order from " + branch.name() + " branch");
        System.out.println("This functionality is not fully implemented in the CLI.");
    }
}
