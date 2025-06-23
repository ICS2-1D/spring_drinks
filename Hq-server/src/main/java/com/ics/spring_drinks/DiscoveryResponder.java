package com.ics.spring_drinks;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

@Component
public class DiscoveryResponder {
    private static final int DISCOVERY_PORT = 8888;
    private static final String DISCOVERY_MESSAGE = "DISCOVER_HQ";

    @PostConstruct
    public void startResponder() {
        new Thread(this::runDiscoveryService).start();
    }

    private void runDiscoveryService() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            System.out.println("✅ Discovery service started on UDP port " + DISCOVERY_PORT);

            byte[] buffer = new byte[256];

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String message = new String(request.getData(), 0, request.getLength()).trim();

                if (DISCOVERY_MESSAGE.equals(message)) {
                    sendDiscoveryResponse(socket, request);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Discovery service error: " + e.getMessage());
        }
    }

// In DiscoveryResponder.java

    private void sendDiscoveryResponse(DatagramSocket socket, DatagramPacket request)
            throws IOException {

        // Check if the discovery request came from the same machine (localhost)
        boolean isLocalClient = request.getAddress().isLoopbackAddress() ||
                request.getAddress().isAnyLocalAddress();

        // If the client is local, tell it to connect to localhost. Otherwise, use the server's public IP.
        String serverAddress = isLocalClient
                ? "127.0.0.1"
                : InetAddress.getLocalHost().getHostAddress();

        String response = "HQ:" + serverAddress + ":9999";

        byte[] responseData = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(
                responseData, responseData.length,
                request.getAddress(), request.getPort()
        );

        socket.send(responsePacket);
        System.out.println("✅ Responded to discovery request from " + request.getAddress().getHostAddress() + " with server address " + serverAddress);
    }
}