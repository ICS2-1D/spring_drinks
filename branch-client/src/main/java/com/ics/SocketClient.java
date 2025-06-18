package com.ics;


import com.ics.dtos.Request;
import com.ics.dtos.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private final String host;
    private final int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Sends a request to the server and returns the response.
     *
     * @param request The request object to send.
     * @return The response from the server.
     */
    public Response sendRequest(Request request) {
        // The try-with-resources statement ensures that each resource is closed at the end of the statement.
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {
            // 1. Send the request to the server
            oos.writeObject(request);
            oos.flush(); // Ensure data is sent

            System.out.println("-> Sent Request: " + request.getType());

            // 2. Wait for and read the response from the server
            Response response = (Response) ois.readObject();
            System.out.println("<- Received Response: " + response.getStatus() + " | Message: " + response.getMessage());

            return response;

        } catch (Exception e) {
            // In case of any exception (e.g., connection refused, serialization error),
            // return a generic error response.
            System.err.println("❌ Client Error: " + e.getMessage());
            e.printStackTrace();
            return new Response(Response.Status.ERROR, null, "Could not connect to server: " + e.getMessage());
        }
    }
}
