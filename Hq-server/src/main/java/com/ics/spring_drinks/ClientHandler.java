package com.ics.spring_drinks;

import com.ics.dtos.*;
import com.ics.models.Branch;
import com.ics.models.OrderStatus;
import com.ics.spring_drinks.services.OrderService;
import com.ics.spring_drinks.services.AdminService;
import com.ics.spring_drinks.services.PaymentService;
import com.ics.spring_drinks.services.ReportService;
import com.ics.spring_drinks.services.DrinkService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.out;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final DrinkService drinkService;
    private final OrderService orderService;
    private final AdminService adminService;
    private final PaymentService paymentService;
    private final ReportService reportService;


    public ClientHandler(Socket socket, DrinkService drinkService, OrderService orderService,
                         AdminService adminService, PaymentService paymentService, ReportService reportService) {
        this.clientSocket = socket;
        this.drinkService = drinkService;
        this.orderService = orderService;
        this.adminService = adminService;
        this.paymentService = paymentService;
        this.reportService = reportService;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            assignBranchToClient();
            // 1. Read the request from the client
            Request request = (Request) ois.readObject();
            out.println("SERVER: Received request of type: " + request.getType());

            // 2. Route the request and prepare a response
            Response response = routeRequest(request);

            // 3. Send the response back to the client
            oos.writeObject(response);
            oos.flush();

        } catch (Exception e) {
            System.err.println("SERVER: Error handling client " + clientSocket.getInetAddress() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                Branch removed = branchAssignments.remove(clientSocket.getInetAddress().getHostAddress());
                assignedBranches.remove(removed);
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("SERVER: Error closing client socket: " + e.getMessage());
            }
        }
    }
    private Map<String, Branch> branchAssignments = new ConcurrentHashMap<>();
    private Set<Branch> assignedBranches = ConcurrentHashMap.newKeySet();

    private Branch assignNextAvailableBranch() {
        // Get all branches from service
        List<Branch> allBranches = List.of(Branch.values());

        // Find first unassigned branch
        return allBranches.stream()
                .filter(branch -> !assignedBranches.contains(branch))
                .findFirst()
                .orElse(null);
    }

    private void assignBranchToClient() throws IOException {
        Branch assignedBranch = assignNextAvailableBranch();
        if (assignedBranch == null) {
            throw new IOException("No available branches to assign");
        }

        branchAssignments.put(clientSocket.getInetAddress().getHostAddress(), assignedBranch);
        assignedBranches.add(assignedBranch);
        out.println("SERVER: Assigned branch " + assignedBranch.name() + " to client " + clientSocket.getInetAddress());
    }


    /**
     * Routes the request to the appropriate service method based on its type.
     * This is where you map request types to your business logic.
     */
    private Response routeRequest(Request request) {
        try {
            switch (request.getType()) {
                // --- Client CLI Requests ---
                case "GET_ALL_DRINKS":
                    return new Response(Response.Status.SUCCESS, drinkService.getAllDrinks(), "Drinks retrieved.");

                case "CREATE_ORDER":
                    OrderRequest orderPayload = (OrderRequest) request.getPayload();
                    OrderResponse newOrder = orderService.createOrder(orderPayload);
                    return new Response(Response.Status.SUCCESS, newOrder, "Order created successfully.");

                case "UPDATE_ORDER_STATUS":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> statusUpdate = (Map<String, Object>) request.getPayload();
                    int orderId = Math.toIntExact(Long.parseLong(statusUpdate.get("orderId").toString()));
                    String orderStatus = (String) statusUpdate.get("orderStatus");

                    orderService.changeOrderStatusAndUpdateInventory(orderId, OrderStatus.valueOf(orderStatus));
                    return new Response(Response.Status.SUCCESS, null, "Order status updated successfully.");

                case "CREATE_PAYMENT":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> paymentData = (Map<String, Object>) request.getPayload();

                    // Convert Map to PaymentRequest
                    PaymentRequest paymentRequest = new PaymentRequest();
                    paymentRequest.setOrderId(Long.parseLong(paymentData.get("orderId").toString()));
                    paymentRequest.setCustomerNumber(paymentData.get("customerNumber").toString());
                    paymentRequest.setPaymentMethod(paymentData.get("paymentMethod").toString());
                    paymentRequest.setPaymentStatus(paymentData.get("paymentStatus").toString());

                    PaymentResponse response = paymentService.processPayment(paymentRequest);

                    return new Response(Response.Status.SUCCESS, response, "Payment recorded successfully.");

                // --- Admin CLI Requests ---
                case "LOGIN_ADMIN":
                    @SuppressWarnings("unchecked")
                    RegisterRequest loginRequest = (RegisterRequest) request.getPayload();


                    String username = loginRequest.getUsername();
                    String password = loginRequest.getPassword();

                    try {
                        String authToken = adminService.login(username, password);
                        return new Response(Response.Status.SUCCESS, authToken, "Login successful.");
                    } catch (IllegalArgumentException ex) {
                        return new Response(Response.Status.ERROR, null, ex.getMessage());
                    }
                case "REGISTER_ADMIN":
                    if (request.getPayload() instanceof RegisterRequest signupRequest) {
                        try {
                            adminService.registerAdmin(signupRequest);
                            return new Response(Response.Status.SUCCESS, null, "Admin registered successfully.");
                        } catch (IllegalArgumentException ex) {
                            return new Response(Response.Status.ERROR, null, ex.getMessage());
                        } catch (Exception e) {
                            return new Response(Response.Status.ERROR, null, "Server error during registration.");
                        }
                    } else {
                        return new Response(Response.Status.ERROR, null, "Invalid payload for REGISTER_ADMIN.");
                    }



                case "ADD_DRINK":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> drinkMap = (Map<String, Object>) request.getPayload();

                    DrinkDto drinkToAdd = new DrinkDto();
                    drinkToAdd.setDrinkName(drinkMap.get("drinkName").toString());
                    drinkToAdd.setDrinkPrice(Double.parseDouble(drinkMap.get("drinkPrice").toString()));
                    drinkToAdd.setDrinkQuantity(Integer.parseInt(drinkMap.get("drinkQuantity").toString()));

                    DrinkDto addedDrink = drinkService.createDrink(drinkToAdd);
                    return new Response(Response.Status.SUCCESS, addedDrink, "Drink added successfully.");

                case "UPDATE_DRINK":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> updateData = (Map<String, Object>) request.getPayload();

//

                    Long drinkId = Long.valueOf(updateData.get("drinkId").toString());

                    DrinkDto drinkUpdate = new DrinkDto();
                    if (updateData.containsKey("drinkPrice")) {
                        drinkUpdate.setDrinkPrice(Double.parseDouble(updateData.get("drinkPrice").toString()));
                    }
                    if (updateData.containsKey("drinkQuantity")) {
                        drinkUpdate.setDrinkQuantity(Integer.parseInt(updateData.get("drinkQuantity").toString()));
                    }

                    DrinkDto updatedDrink = drinkService.updateDrink(drinkId, drinkUpdate);
                    return new Response(Response.Status.SUCCESS, updatedDrink, "Drink updated successfully.");

                case "GET_SALES_REPORT":
                    try {
                        Object salesReport = reportService.buildSalesReport();
                        return new Response(Response.Status.SUCCESS, salesReport, "Sales report generated.");
                    } catch (Exception e) {
                        e.printStackTrace(); // Optional: log or handle
                        return new Response(Response.Status.ERROR, null, "Failed to generate report: " + e.getMessage());
                    }

//                case "SYNC_TO_HQ":
//                    @SuppressWarnings("unchecked")
//                    Map<String, Object> syncData = (Map<String, Object>) request.getPayload();
//
//                    // Validate auth token
//                    String syncToken = (String) syncData.get("authToken");
//                    if (!adminService.validateToken(syncToken)) {
//                        return new Response(Response.Status.ERROR, null, "Unauthorized: Invalid token.");
//                    }
//
//                    String branchId = (String) syncData.get("branchId");
//                    boolean synced = reportService.syncToHQ(branchId);
//                    if (synced) {
//                        return new Response(Response.Status.SUCCESS, null, "Data synced to HQ successfully.");
//                    } else {
//                        return new Response(Response.Status.ERROR, null, "Failed to sync data to HQ.");
//                    }

                default:
                    return new Response(Response.Status.ERROR, null, "Unknown request type: " + request.getType());
            }
        } catch (ClassCastException e) {
            return new Response(Response.Status.ERROR, null, "Invalid request payload format: " + e.getMessage());
        } catch (Exception e) {
            // Catch exceptions from the service layer (e.g., validation, database errors)
            return new Response(Response.Status.ERROR, null, "Server-side error: " + e.getMessage());
        }
    }
}