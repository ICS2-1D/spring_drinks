package com.ics.spring_drinks.controllers;


import com.ics.dtos.DrinkDto;
import com.ics.dtos.RegisterRequest;
import com.ics.models.Branch;
import com.ics.spring_drinks.BranchManager;
import com.ics.spring_drinks.services.AdminService;
import com.ics.spring_drinks.services.DrinkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final DrinkService drinkService;
    private final BranchManager branchManager;

    @GetMapping("/connect")
    public ResponseEntity<?> handleConnection(HttpServletRequest request) {
        try {
            String clientId = request.getSession().getId();
            InetAddress clientAddress = InetAddress.getByName(request.getRemoteAddr());
            // False for web clients, true for TCP admin clients
            Branch assignedBranch = branchManager.assignBranch(clientId, clientAddress, false);
            Map<String, String> response = Collections.singletonMap("branch", assignedBranch.name());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during connection.");
        }
    }


    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            adminService.registerAdmin(registerRequest);
            return ResponseEntity.ok("Admin registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> login(@RequestBody RegisterRequest registerRequest) {
        try {
            String response = adminService.login(registerRequest.getUsername(), registerRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/admin/drinks/{id}")
    public ResponseEntity<?> updateDrink(@PathVariable Long id, @RequestBody DrinkDto updatedDrinkDto) {
        try {
            // Admin updates are assumed to be for the main NAIROBI branch inventory
            drinkService.updateDrink(id, updatedDrinkDto, Branch.NAIROBI);

            System.out.println("Updating drink " + id + " at NAIROBI -> Price: " + updatedDrinkDto.getDrinkPrice() +
                    ", Quantity: " + updatedDrinkDto.getDrinkQuantity());

            return ResponseEntity.ok("✅ Drink updated successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Drink not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error updating drink: " + e.getMessage());
        }
    }

    /**
     * NEW: Endpoint to handle the GET request for low stock items from the admin frontend.
     */
    @GetMapping("/admin/low-stock")
    public ResponseEntity<?> getLowStockItems() {
        try {
            List<DrinkDto> lowStockItems = drinkService.getLowStockItems();
            return ResponseEntity.ok(lowStockItems);
        } catch (Exception e) {
            System.err.println("API Error fetching low stock items: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching low stock items: " + e.getMessage());
        }
    }
}
