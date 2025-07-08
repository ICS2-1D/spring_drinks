package com.ics.spring_drinks.controllers;

import com.ics.dtos.DrinkDto;
import com.ics.models.Branch;
import com.ics.spring_drinks.services.DrinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkService drinkService;

    @GetMapping
    public ResponseEntity<List<DrinkDto>> getAllDrinks(@RequestParam Branch branch) {
        return ResponseEntity.ok(drinkService.getAllDrinks(branch));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrinkDto> getDrinkById(@PathVariable Long id, @RequestParam Branch branch) {
        return ResponseEntity.ok(drinkService.getDrinkById(id, branch));
    }

    @PostMapping
    public ResponseEntity<DrinkDto> createDrink(@RequestBody DrinkDto drinkDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drinkService.createDrink(drinkDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DrinkDto> updateDrink(@PathVariable Long id, @RequestBody DrinkDto drinkDto, @RequestParam Branch branch) {
        return ResponseEntity.ok(drinkService.updateDrink(id, drinkDto, branch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrink(@PathVariable Long id) {
        drinkService.deleteDrink(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restock")
    public ResponseEntity<Void> restockDrink(@RequestParam long drinkId, @RequestParam Branch branch, @RequestParam int quantity) {
        drinkService.restockDrink(drinkId, branch, quantity);
        return ResponseEntity.ok().build();
    }
}