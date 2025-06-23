package com.ics.spring_drinks;

import com.ics.spring_drinks.services.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TcpServerService {
    private static final int PORT = 9090;
    private ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    private final BranchManager branchManager;
    private final ServiceProvider services;

    public TcpServerService(DrinkService drinkService, OrderService orderService,
                            AdminService adminService, PaymentService paymentService,
                            ReportService reportService, BranchManager branchManager) {
        this.branchManager = branchManager;
        this.services = new ServiceProvider(drinkService, orderService, adminService,
                paymentService, reportService);
    }

    @PostConstruct
    private void startServer() {
        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("✅ TCP Server started on port " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("🔗 New client: " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(clientSocket, branchManager, services);
                    executor.submit(handler);

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
        System.out.println("✅ TCP server stopped");
    }
}