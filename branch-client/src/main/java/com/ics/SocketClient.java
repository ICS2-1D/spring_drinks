package com.ics;


import com.ics.dtos.Request;
import com.ics.dtos.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient {

    private final String serverIp;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    //discover server
    public SocketClient() throws IOException {
        this.serverIp = discoverServer();
        System.out.println("Server at " + serverIp);
    }

    public String discoverServer() throws IOException {
        DatagramSocket discoverySocket = new DatagramSocket();

        try (discoverySocket) {
            discoverySocket.setBroadcast(true);
            discoverySocket.setSoTimeout(5000);

            byte[] sendData = "DISCOVER_HQ".getBytes();
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket discoveryPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, 8888);
            discoverySocket.send(discoveryPacket);

            System.out.println("📡 Discovery message sent");

            // 2. Wait for a response
            byte[] buffer = new byte[256];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            discoverySocket.receive(response);

            //parse the response
            String responseMessage = new String(response.getData(), 0, response.getLength());
            String[] parts = responseMessage.split(":");

            if (parts.length >= 2 && parts[0].equals("HQ")) {
                return parts[1];
            } else {
                throw new IOException("Invalid response from server: " + responseMessage);
            }

        } catch (IOException e) {
            System.out.println("No server found: " + e.getMessage());
            throw e;
        }
    }

    //connect method to establish a connection to the server
    public void connect() throws Exception {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        socket = new Socket(serverIp,9090);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());

        System.out.println("Connected to server at " + serverIp + ":9090");
    }

    // Method to send an object to the server and receive a response
    public Response sendRequest(Request request) {
        try {
            if (socket == null || socket.isClosed()) {
                connect();
            }
            output.writeObject(request);
            output.flush();

            return (Response) input.readObject();
        } catch (Exception e) {
            System.out.println("Error sending request: " + e.getMessage());

            try {
                disconnect();
                connect();

                output.writeObject(request);
                output.flush();

                return (Response) input.readObject();
            } catch (Exception retryException) {
                System.out.println("Retry failed: " + retryException.getMessage());
                return new Response(Response.Status.ERROR, null,
                        "Failed to send request after retry: " + retryException.getMessage());
            }
        }
    }

    // Method to disconnect from the server
    public void disconnect() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.out.println("Error disconnecting: " + e.getMessage());
        }
    }

    //get discovery ip
    public String getServerIp() {
        return serverIp;
    }

    //check if the client is connected
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}