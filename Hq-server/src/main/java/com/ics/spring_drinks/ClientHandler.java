package com.ics.spring_drinks;


import com.ics.spring_drinks.services.OrderService;
import com.ics.dtos.Request;
import com.ics.dtos.Response;
import com.ics.dtos.DrinkDto; // Assuming you have this DTO
import com.ics.spring_drinks.services.DrinkService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final DrinkService drinkService;
    private final OrderService orderService;
    // Add other services like AdminService as needed

    public ClientHandler(Socket socket, DrinkService drinkService, OrderService orderService) {
        this.clientSocket = socket;
        this.drinkService = drinkService;
        this.orderService = orderService;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // 1. Read the request from the client
            Request request = (Request) ois.readObject();
            System.out.println("SERVER: Received request of type: " + request.getType());

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
                clientSocket.close(); // Always close the socket
            } catch (Exception e) {
                System.err.println("SERVER: Error closing client socket: " + e.getMessage());
            }
        }
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
                    // The service method should return a List<DrinkDto>
                    return new Response(Response.Status.SUCCESS, drinkService.getAllDrinks(), "Drinks retrieved.");

                case "CREATE_ORDER":
                    // Your OrderService would handle this, perhaps taking an OrderRequest DTO
                    // OrderRequest orderPayload = (OrderRequest) request.getPayload();
                    // Order newOrder = orderService.createNewOrder(orderPayload);
                    // return new Response(Response.Status.SUCCESS, newOrder, "Order created.");
                    // For now, a placeholder:
                    return new Response(Response.Status.SUCCESS, null, "Order created successfully (simulated).");

                // --- Admin CLI Requests ---
                case "ADD_DRINK":
                    DrinkDto drinkPayload = (DrinkDto) request.getPayload();
                    // Your DrinkService should have a method that takes a DTO
                    // drinkService.addNewDrink(drinkPayload);
                    return new Response(Response.Status.SUCCESS, null, "Drink added successfully.");

                // Add more cases for LOGIN_ADMIN, UPDATE_DRINK, GET_ALL_ORDERS, etc.

                default:
                    return new Response(Response.Status.ERROR, null, "Unknown request type: " + request.getType());
            }
        } catch (Exception e) {
            // Catch exceptions from the service layer (e.g., validation, database errors)
            return new Response(Response.Status.ERROR, null, "Server-side error: " + e.getMessage());
        }
    }
}

