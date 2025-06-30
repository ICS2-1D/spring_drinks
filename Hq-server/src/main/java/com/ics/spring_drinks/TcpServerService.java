package com.ics.spring_drinks;

import com.ics.spring_drinks.services.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TcpServerService {
    private static final int PORT = 9090;
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    private static final int MAX_THREADS = 10; // Limit concurrent connections

    private ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    private volatile boolean running = true;

    private final BranchManager branchManager;
    private final ServiceProvider services;

    @Value("${server.admin.ip:}")
    private String adminIpAddress;

    public TcpServerService(DrinkService drinkService, OrderService orderService,
                            AdminService adminService, PaymentService paymentService,
                            ReportService reportService, BranchManager branchManager) {
        this.branchManager = branchManager;
        this.services = new ServiceProvider(drinkService, orderService, adminService,
                paymentService, reportService);
    }

    @PostConstruct
    private void startServer() {

        if (adminIpAddress != null && !adminIpAddress.isEmpty()) {
            System.out.println("🎯 Admin IP configured: " + adminIpAddress);
        }

        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(1000);
            System.out.println("✅ TCP Server started on port " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(SOCKET_TIMEOUT);

                    String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                    System.out.println("🔗 New client connection: " + clientInfo);

                    if (hasAvailableBranches()) {
                        ClientHandler handler = new ClientHandler(clientSocket, branchManager, services);
                        executor.submit(handler);
                    } else {
                        System.out.println("❌ No available branches for client: " + clientInfo);
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }

                } catch (SocketTimeoutException e) {
                    // Normal timeout for checking shutdown - continue
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Client connection error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Server startup failed: " + e.getMessage());
        }
    }

    private boolean hasAvailableBranches() {
        return branchManager.getAssignedBranches().size() < com.ics.models.Branch.values().length;
    }

    @PreDestroy
    private void stopServer() {
        System.out.println("🛑 Stopping TCP server...");
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Error closing server: " + e.getMessage());
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        System.out.println("✅ TCP server stopped");
    }
}