package com.ics.spring_drinks.controllers;


import com.ics.dtos.DrinkDto;
import com.ics.dtos.RegisterRequest;
import com.ics.dtos.SalesReportDto;
import com.ics.spring_drinks.services.AdminService;
import com.ics.spring_drinks.services.DrinkService;
import com.ics.spring_drinks.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final DrinkService drinkService;
    private final ReportService reportService;


    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            adminService.registerAdmin(registerRequest);
            return ResponseEntity.ok("Admin registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RegisterRequest registerRequest) {
        try {
            String response = adminService.login(registerRequest.getUsername(), registerRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build() ;
        }
    }

    // TODO: Implement this endpoint in your AdminService and a corresponding repository method
    @PutMapping("/drinks/{id}/price")
    public ResponseEntity<?> updateDrinkPrice(@PathVariable Long id, @RequestBody DrinkDto updatedDrinkDto) {
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

    // TODO: Implement this endpoint in a new SalesReportService or within AdminService
    @GetMapping("/sales/report")
    public ResponseEntity<SalesReportDto> getSalesReport() {
        try {
            SalesReportDto report = reportService.buildSalesReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}
