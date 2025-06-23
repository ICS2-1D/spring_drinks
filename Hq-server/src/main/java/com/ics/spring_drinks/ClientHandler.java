package com.ics.spring_drinks;

import com.ics.dtos.*;
import com.ics.models.Branch;
import com.ics.models.OrderStatus;


import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientId;
    private final BranchManager branchManager;
    private final ServiceProvider services;
    private Branch assignedBranch;

    public ClientHandler(Socket socket, BranchManager branchManager, ServiceProvider services) {
        this.clientSocket = socket;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.branchManager = branchManager;
        this.services = services;
    }
    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            // Step 1: Read initial request (CONNECT or CONNECT_ADMIN)
            Object initialRequestObject = in.readObject();
            if (!(initialRequestObject instanceof Request initialRequest)) {
                sendResponse(out, Response.Status.ERROR, null, "Handshake failed: Invalid request object.");
                return;
            }

            boolean isAdmin = false;

            if ("CONNECT_ADMIN".equalsIgnoreCase(initialRequest.getType())) {
                isAdmin = true;
            } else if (!"CONNECT".equalsIgnoreCase(initialRequest.getType())) {
                sendResponse(out, Response.Status.ERROR, null, "Handshake failed: Expected CONNECT or CONNECT_ADMIN.");
                return;
            }

            System.out.println("📨 Initial Request: " + initialRequest.getType() + " from " + clientSocket.getInetAddress().getHostAddress());

            assignedBranch = branchManager.assignBranch(clientId, clientSocket.getInetAddress(), isAdmin);
            sendResponse(out, Response.Status.SUCCESS, assignedBranch, "Connected to branch: " + assignedBranch.name());

            // Step 3: Loop to process ongoing requests
            while (true) {
                Request request = (Request) in.readObject();
                System.out.println("📨 Request: " + request.getType() + " from " + assignedBranch.name());

                if ("EXIT".equalsIgnoreCase(request.getType())) {
                    break;
                }

                Response response = processRequest(request);
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("❌ Client error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }


    private Response processRequest(Request request) {
        try {
            // Note: "CONNECT" is correctly handled outside this switch now.
            return switch (request.getType()) {
                case "GET_BRANCH_INFO" -> handleBranchInfo();
                case "GET_ALL_DRINKS" -> handleGetDrinks();
                case "CREATE_ORDER" -> handleCreateOrder(request);
                case "UPDATE_ORDER_STATUS" -> handleUpdateOrderStatus(request);
                case "CREATE_PAYMENT" -> handleCreatePayment(request);
                case "LOGIN_ADMIN" -> handleAdminLogin(request);
                case "REGISTER_ADMIN" -> handleAdminRegister(request);
                case "ADD_DRINK" -> handleAddDrink(request);
                case "UPDATE_DRINK" -> handleUpdateDrink(request);
                case "GET_SALES_REPORT" -> handleSalesReport(request);
                default -> errorResponse("Unknown request type: " + request.getType());
            };
        } catch (Exception e) {
            return errorResponse("Server error: " + e.getMessage());
        }
    }

    // Branch operations
    private Response handleBranchInfo() {
        return successResponse(assignedBranch, "Branch info for " + assignedBranch.name());
    }

    // Drink operations
    private Response handleGetDrinks() {
        return successResponse(services.drinkService().getAllDrinks(), "Drinks retrieved");
    }

    private Response handleAddDrink(Request request) {
        if (!isAdminBranch()) return adminOnlyError();

        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        DrinkDto drink = createDrinkFromMap(data);
        DrinkDto result = services.drinkService().createDrink(drink);
        return successResponse(result, "Drink added successfully");
    }

    private Response handleUpdateDrink(Request request) {
        if (!isAdminBranch()) return adminOnlyError();

        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        Long drinkId = Long.valueOf(data.get("drinkId").toString());
        DrinkDto updates = createDrinkUpdateFromMap(data);
        DrinkDto result = services.drinkService().updateDrink(drinkId, updates);
        return successResponse(result, "Drink updated successfully");
    }

    // Order operations
    private Response handleCreateOrder(Request request) {
        OrderRequest orderRequest = (OrderRequest) request.getPayload();
        orderRequest.setBranch(assignedBranch);
        OrderResponse result = services.orderService().createOrder(orderRequest);
        return successResponse(result, "Order created successfully");
    }

    private Response handleUpdateOrderStatus(Request request) {
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        int orderId = Integer.parseInt(data.get("orderId").toString());
        OrderStatus status = OrderStatus.valueOf(data.get("orderStatus").toString());

        services.orderService().changeOrderStatusAndUpdateInventory(orderId, status);
        return successResponse(null, "Order status updated");
    }

    // Payment operations
    private Response handleCreatePayment(Request request) {
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        PaymentRequest paymentRequest = createPaymentFromMap(data);
        PaymentResponse result = services.paymentService().processPayment(paymentRequest);
        return successResponse(result, "Payment processed successfully");
    }

    // Admin operations
    private Response handleAdminLogin(Request request) {
        if (!isAdminBranch()) return adminOnlyError();

        RegisterRequest loginRequest = (RegisterRequest) request.getPayload();
        try {
            String token = services.adminService().login(loginRequest.getUsername(), loginRequest.getPassword());
            return successResponse(token, "Login successful");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage());
        }
    }

    private Response handleAdminRegister(Request request) {
        if (!isAdminBranch()) return adminOnlyError();

        try {
            RegisterRequest registerRequest = (RegisterRequest) request.getPayload();
            services.adminService().registerAdmin(registerRequest);
            return successResponse(null, "Admin registered successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage());
        }
    }


    private Response handleSalesReport(Request request) {
        if (!isAdminBranch()) return adminOnlyError();

        try {
            // Check the request payload to see if a specific branch was requested
            Map<String, Object> data = (Map<String, Object>) request.getPayload();
            Branch targetBranch = null;

            if (data != null && data.containsKey("branch")) {
                try {
                    targetBranch = Branch.valueOf(data.get("branch").toString());
                } catch (IllegalArgumentException e) {
                    return errorResponse("Invalid branch name specified.");
                }
            }

            Object report;
            if (targetBranch != null) {
                // If a branch is specified, get the report for that branch
                report = services.reportService().buildSalesReportForBranch(targetBranch);
            } else {
                // Otherwise, get the full consolidated report
                report = services.reportService().buildConsolidatedReport();
            }

            return successResponse(report, "Sales report generated successfully");
        } catch (Exception e) {
            // It's good practice to log the full error on the server
            System.err.println("❌ Failed to generate report: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Failed to generate report: An internal error occurred.");
        }
    }

    // Helper methods
    private boolean isAdminBranch() {
        return assignedBranch == Branch.NAIROBI;
    }

    private Response adminOnlyError() {
        return errorResponse("Access Denied: Admin actions only allowed from Nairobi branch");
    }

    private Response successResponse(Object data, String message) {
        return new Response(Response.Status.SUCCESS, data, message);
    }

    private Response errorResponse(String message) {
        return new Response(Response.Status.ERROR, null, message);
    }

    private void sendResponse(ObjectOutputStream out, Response.Status status, Object data, String message)
            throws IOException {
        out.writeObject(new Response(status, data, message));
        out.flush();
    }

    private DrinkDto createDrinkFromMap(Map<String, Object> data) {
        DrinkDto drink = new DrinkDto();
        drink.setDrinkName(data.get("drinkName").toString());
        drink.setDrinkPrice(Double.parseDouble(data.get("drinkPrice").toString()));
        drink.setDrinkQuantity(Integer.parseInt(data.get("drinkQuantity").toString()));
        return drink;
    }

    private DrinkDto createDrinkUpdateFromMap(Map<String, Object> data) {
        DrinkDto drink = new DrinkDto();
        if (data.containsKey("drinkPrice")) {
            drink.setDrinkPrice(Double.parseDouble(data.get("drinkPrice").toString()));
        }
        if (data.containsKey("drinkQuantity")) {
            drink.setDrinkQuantity(Integer.parseInt(data.get("drinkQuantity").toString()));
        }
        return drink;
    }

    private PaymentRequest createPaymentFromMap(Map<String, Object> data) {
        PaymentRequest payment = new PaymentRequest();
        payment.setOrderId(Long.parseLong(data.get("orderId").toString()));
        payment.setCustomerNumber(data.get("customerNumber").toString());
        payment.setPaymentMethod(data.get("paymentMethod").toString());
        payment.setPaymentStatus(data.get("paymentStatus").toString());
        return payment;
    }

    private void cleanup() {
        try {
            if (assignedBranch != null) {
                branchManager.unassignBranch(clientId, assignedBranch);
            }
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("❌ Cleanup error: " + e.getMessage());
        }
    }
}