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
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final DrinkService drinkService;
    private final BranchManager branchManager; // Injected BranchManager

    /**
     * NEW: Public endpoint for any client (customer or admin) to connect.
     * This is called by your main index.html page.
     * Path: /connect
     */
    @GetMapping("/connect")
    public ResponseEntity<?> handleConnection(HttpServletRequest request) {
        try {
            String clientId = request.getSession().getId();
            InetAddress clientAddress = InetAddress.getByName(request.getRemoteAddr());
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


    /**
     * Path is now explicitly "/admin/register"
     */
    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            adminService.registerAdmin(registerRequest);
            return ResponseEntity.ok("Admin registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Path is now explicitly "/admin/login"
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> login(@RequestBody RegisterRequest registerRequest) {
        try {
            String response = adminService.login(registerRequest.getUsername(), registerRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Path is now explicitly "/admin/drinks/{id}"
     */
    @PutMapping("/admin/drinks/{id}")
    public ResponseEntity<?> updateDrink(@PathVariable Long id, @RequestBody DrinkDto updatedDrinkDto) {
        try {
            drinkService.updateDrink(id, updatedDrinkDto);
            System.out.println("Updating drink " + id + " -> Price: " + updatedDrinkDto.getDrinkPrice() +
                    ", Quantity: " + updatedDrinkDto.getDrinkQuantity());

            return ResponseEntity.ok("✅ Drink updated successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Drink not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error updating drink");
        }
    }

}
