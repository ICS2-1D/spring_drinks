package com.ics.spring_drinks;

import com.ics.spring_drinks.services.DrinkService;
import com.ics.spring_drinks.services.OrderService;
import com.ics.spring_drinks.services.AdminService;
import com.ics.spring_drinks.services.PaymentService;
import com.ics.spring_drinks.services.ReportService;
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

    private final int port = 9999;
    private ServerSocket serverSocket;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean isRunning = true;

    // Inject all the services that ClientHandler will need
    private final DrinkService drinkService;
    private final OrderService orderService;
    private final AdminService adminService;
    private final PaymentService paymentService;
    private final ReportService reportService;

    public TcpServerService(DrinkService drinkService, OrderService orderService,
                            AdminService adminService, PaymentService paymentService,
                            ReportService reportService) {
        this.drinkService = drinkService;
        this.orderService = orderService;
        this.adminService = adminService;
        this.paymentService = paymentService;
        this.reportService = reportService;
    }

    @PostConstruct
    private void startServer() {
        // Start the server in a new thread to avoid blocking the main Spring Boot thread
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("✅ TCP Socket Server started on port " + port);

                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept(); // Wait for a client to connect
                        System.out.println("-> New client connected: " + clientSocket.getInetAddress());

                        // Create a new handler for the client and execute it in the thread pool
                        ClientHandler clientHandler = new ClientHandler(clientSocket, drinkService, orderService,
                                adminService, paymentService, reportService);
                        executorService.submit(clientHandler);

                    } catch (IOException e) {
                        if (!isRunning) {
                            System.out.println("Server socket closed.");
                        } else {
                            System.err.println("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not start TCP server on port " + port + ": " + e.getMessage());
            }
        }).start();
    }

    @PreDestroy
    private void stopServer() {
        System.out.println("Stopping TCP server...");
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        executorService.shutdown();
    }
}