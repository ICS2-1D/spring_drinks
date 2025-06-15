package com.ics.spring_drinks.controllers;

import com.ics.dtos.DrinkDto;
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
    public ResponseEntity<List<DrinkDto>> getAllDrinks() {
        return ResponseEntity.ok(drinkService.getAllDrinks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrinkDto> getDrinkById(@PathVariable Long id) {
        return ResponseEntity.ok(drinkService.getDrinkById(id));
    }

    @PostMapping
    public ResponseEntity<DrinkDto> createDrink(@RequestBody DrinkDto drinkDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drinkService.createDrink(drinkDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DrinkDto> updateDrink(@PathVariable Long id, @RequestBody DrinkDto drinkDto) {
        return ResponseEntity.ok(drinkService.updateDrink(id, drinkDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrink(@PathVariable Long id) {
        drinkService.deleteDrink(id);
        return ResponseEntity.noContent().build();
    }
}


