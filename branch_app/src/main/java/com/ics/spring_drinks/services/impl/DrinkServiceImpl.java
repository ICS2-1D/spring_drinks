package com.ics.spring_drinks.services.impl;

import com.ics.dtos.DrinkDto;
import com.ics.models.Drink;
import com.ics.spring_drinks.repository.DrinkRepository;
import com.ics.spring_drinks.services.DrinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private final DrinkRepository drinkRepository;

    @Override
    public List<DrinkDto> getAllDrinks() {
        return drinkRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public DrinkDto getDrinkById(long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + id));
        return mapToDto(drink);
    }

    @Override
    public DrinkDto createDrink(DrinkDto drinkDto) {
        Drink drink = new Drink();
        drink.setDrinkName(drinkDto.getDrinkName());
        drink.setDrinkPrice(drinkDto.getDrinkPrice());
        drink.setDrinkQuantity(drinkDto.getDrinkQuantity());

        Drink saved = drinkRepository.save(drink);
        return mapToDto(saved);
    }

    @Override
    public DrinkDto updateDrink(long id, DrinkDto drinkDto) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + id));

        drink.setDrinkName(drinkDto.getDrinkName());
        drink.setDrinkPrice(drinkDto.getDrinkPrice());
        drink.setDrinkQuantity(drinkDto.getDrinkQuantity());

        Drink updated = drinkRepository.save(drink);
        return mapToDto(updated);
    }

    @Override
    public void deleteDrink(long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + id));
        drinkRepository.delete(drink);
    }

    // 🔁 Mapper method
    private DrinkDto mapToDto(Drink drink) {
        return new DrinkDto(
                drink.getId(),
                drink.getDrinkName(),
                drink.getDrinkQuantity(),
                drink.getDrinkPrice()
        );
    }
}
