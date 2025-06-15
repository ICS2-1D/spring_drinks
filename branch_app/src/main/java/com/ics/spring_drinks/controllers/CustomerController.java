//package com.ics.spring_drinks.controllers;
//
//import com.ics.dtos.CustomerDto;
//import com.ics.models.Customer;
//import com.ics.spring_drinks.services.CustomerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RequiredArgsConstructor
//@RequestMapping("/customer")
//@RestController
//public class CustomerController {
//
//    private final CustomerService customerService;
//
//    // POST /customer - Create a new customer
//    @PostMapping
//    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerDto customerDto) {
//        Customer customer = customerService.createCustomer(customerDto);
//        return ResponseEntity.ok(customer);
//    }
//
//    // GET /customer/{id} - Get customer by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Customer> getCustomerById(@PathVariable int id) {
//        Customer customer = customerService.getCustomerById(id);
//        if (customer != null) {
//            return ResponseEntity.ok(customer);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // GET /customer - Get all customers
//    @GetMapping
//    public ResponseEntity<?> getAllCustomers() {
//        try {
//            return ResponseEntity.ok(customerService.getAllCustomers());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("An error occurred while fetching customers.");
//        }
//    }
//}