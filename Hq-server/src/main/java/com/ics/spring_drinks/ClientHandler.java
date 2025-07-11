package com.ics.spring_drinks;

import com.ics.dtos.DrinkDto;
import com.ics.dtos.OrderRequest;
import com.ics.dtos.OrderResponse;
import com.ics.dtos.PaymentRequest;
import com.ics.dtos.PaymentResponse;
import com.ics.dtos.RegisterRequest;
import com.ics.dtos.Request;
import com.ics.dtos.Response;
import com.ics.models.Branch;
import com.ics.models.OrderStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientId;
    private final BranchManager branchManager;
    private final ServiceProvider services;
    private Branch assignedBranch;
    private static final int CLIENT_TIMEOUT = 90000;

    public ClientHandler(Socket socket, BranchManager branchManager, ServiceProvider services) {
        this.clientSocket = socket;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.branchManager = branchManager;
        this.services = services;
    }

    @Override
    public void run() {
        try {
            clientSocket.setSoTimeout(CLIENT_TIMEOUT);

            try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                System.out.println("📡 Connection established with " + clientId);

                Object initialRequestObject = in.readObject();
                if (!(initialRequestObject instanceof Request initialRequest)) {
                    sendResponse(out, Response.Status.ERROR, null, "Handshake failed: Invalid request object.");
                    return;
                }

                boolean isAdmin = "CONNECT_ADMIN".equalsIgnoreCase(initialRequest.getType());

                if (!isAdmin && !"CONNECT".equalsIgnoreCase(initialRequest.getType())) {
                    sendResponse(out, Response.Status.ERROR, null, "Handshake failed: Expected CONNECT or CONNECT_ADMIN.");
                    return;
                }

                System.out.println("📨 Initial Request: " + initialRequest.getType() + " from " + clientSocket.getInetAddress().getHostAddress());

                try {
                    assignedBranch = branchManager.assignBranch(clientId, clientSocket.getInetAddress(), isAdmin);
                    sendResponse(out, Response.Status.SUCCESS, assignedBranch, "Connected to branch: " + assignedBranch.name());
                } catch (IOException e) {
                    sendResponse(out, Response.Status.ERROR, null, e.getMessage());
                    return;
                }

                while (true) {
                    try {
                        Request request = (Request) in.readObject();
                        System.out.println("📨 Request: " + request.getType() + " from " + assignedBranch.name());

                        if ("EXIT".equalsIgnoreCase(request.getType())) {
                            break;
                        }

                        Response response = processRequest(request);
                        out.writeObject(response);
                        out.flush();
                    } catch (java.net.SocketTimeoutException e) {
                        System.out.println("⏰ Client timeout: " + clientId);
                        break;
                    } catch (java.io.EOFException e) {
                        System.out.println("🔌 Client disconnected: " + clientId);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Client error (" + clientId + "): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private Response processRequest(Request request) {
        try {
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
                case "GET_LOW_STOCK" -> handleGetLowStock();
                case "RESTOCK_DRINK" -> handleRestockDrink(request);
                default -> errorResponse("Unknown request type: " + request.getType());
            };
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse("Server error: " + e.getMessage());
        }
    }

    private Response handleGetLowStock() {
        if (!isAdminBranch()) return adminOnlyError();
        try {
            List<DrinkDto> lowStockItems = services.drinkService().getLowStockItems();
            return successResponse(lowStockItems, "Low stock items retrieved successfully.");
        } catch (Exception e) {
            System.err.println("❌ Error getting low stock items: " + e.getMessage());
            return errorResponse("Failed to retrieve low stock items.");
        }
    }

    private Response handleRestockDrink(Request request) {
        if (!isAdminBranch()) return adminOnlyError();
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        long drinkId = Long.parseLong(data.get("drinkId").toString());
        Branch branch = Branch.valueOf(data.get("branch").toString());
        int quantity = Integer.parseInt(data.get("quantity").toString());
        services.drinkService().restockDrink(drinkId, branch, quantity);
        return successResponse(null, "Drink restocked successfully");
    }

    private Response handleBranchInfo() {
        return successResponse(assignedBranch, "Branch info for " + assignedBranch.name());
    }

    private Response handleGetDrinks() {
        return successResponse(services.drinkService().getAllDrinks(assignedBranch), "Drinks retrieved");
    }

    private Response handleCreateOrder(Request request) {
        OrderRequest orderRequest = (OrderRequest) request.getPayload();
        orderRequest.setBranch(assignedBranch);
        OrderResponse result = services.orderService().createOrder(orderRequest);
        return successResponse(result, "Order created successfully");
    }

    private Response handleAddDrink(Request request) {
        if (!isAdminBranch()) return adminOnlyError();
        try {
            DrinkDto drink = createDrinkFromMap((Map<String, Object>) request.getPayload());
            DrinkDto result = services.drinkService().createDrink(drink);
            return successResponse(result, "Drink added successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage());
        }
    }

    private Response handleUpdateDrink(Request request) {
        if (!isAdminBranch()) return adminOnlyError();
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        Long drinkId = Long.valueOf(data.get("drinkId").toString());
        DrinkDto updates = createDrinkUpdateFromMap(data);
        DrinkDto result = services.drinkService().updateDrink(drinkId, updates, assignedBranch);
        return successResponse(result, "Drink updated successfully");
    }

    private Response handleUpdateOrderStatus(Request request) {
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        int orderId = Integer.parseInt(data.get("orderId").toString());
        OrderStatus status = OrderStatus.valueOf(data.get("orderStatus").toString());
        services.orderService().changeOrderStatusAndUpdateInventory(orderId, status);
        return successResponse(null, "Order status updated");
    }

    private Response handleCreatePayment(Request request) {
        PaymentRequest paymentRequest = createPaymentFromMap((Map<String, Object>) request.getPayload());
        PaymentResponse result = services.paymentService().processPayment(paymentRequest);
        return successResponse(result, "Payment processed successfully");
    }

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
            services.adminService().registerAdmin((RegisterRequest) request.getPayload());
            return successResponse(null, "Admin registered successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage());
        }
    }

    private Response handleSalesReport(Request request) {
        if (!isAdminBranch()) return adminOnlyError();
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        Branch targetBranch = data != null && data.containsKey("branch") ? Branch.valueOf(data.get("branch").toString()) : null;
        Object report = targetBranch != null ? services.reportService().buildSalesReportForBranch(targetBranch) : services.reportService().buildConsolidatedReport();
        return successResponse(report, "Sales report generated successfully");
    }

    private boolean isAdminBranch() {
        return assignedBranch == Branch.NAIROBI;
    }

    private Response adminOnlyError() {
        return new Response(Response.Status.ERROR, null, "Access Denied: Admin actions only allowed from Nairobi branch");
    }

    private Response successResponse(Object data, String message) {
        return new Response(Response.Status.SUCCESS, data, message);
    }

    private Response errorResponse(String message) {
        return new Response(Response.Status.ERROR, null, message);
    }

    private void sendResponse(ObjectOutputStream out, Response.Status status, Object data, String message) throws IOException {
        out.writeObject(new Response(status, data, message));
        out.flush();
    }

    /**
     * CORRECTED: This method is now robust and validates the incoming data.
     * It checks for the existence of required keys and handles potential exceptions.
     */
    private DrinkDto createDrinkFromMap(Map<String, Object> data) {
        if (data == null || !data.containsKey("drinkName") || !data.containsKey("drinkPrice") || !data.containsKey("drinkQuantity")) {
            throw new IllegalArgumentException("Payload for creating a drink must contain drinkName, drinkPrice, and drinkQuantity.");
        }
        try {
            String drinkName = data.get("drinkName").toString();
            if (drinkName == null || drinkName.trim().isEmpty()) {
                throw new IllegalArgumentException("Drink name cannot be empty.");
            }
            int quantity = Integer.parseInt(data.get("drinkQuantity").toString());
            double price = Double.parseDouble(data.get("drinkPrice").toString());

            if (price < 0 || quantity < 0) {
                throw new IllegalArgumentException("Price and quantity cannot be negative.");
            }

            return new DrinkDto(null, drinkName, quantity, price, null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for price or quantity.", e);
        }
    }

    private DrinkDto createDrinkUpdateFromMap(Map<String, Object> data) {
        DrinkDto drink = new DrinkDto();
        if (data.containsKey("drinkPrice")) drink.setDrinkPrice(Double.parseDouble(data.get("drinkPrice").toString()));
        if (data.containsKey("drinkQuantity")) drink.setDrinkQuantity(Integer.parseInt(data.get("drinkQuantity").toString()));
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
                System.out.println("🧹 Cleaning up TCP client and unassigning branch for IP: " + clientSocket.getInetAddress().getHostAddress());
                branchManager.unassignBranch(clientSocket.getInetAddress());
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Cleanup error: " + e.getMessage());
        }
    }
}
