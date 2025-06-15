package com.ics.spring_drinks.services;

import com.ics.spring_drinks.dtos.DrinkDto;
import com.ics.spring_drinks.models.Drink;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DrinkService {
   List<DrinkDto> getAllDrinks();
   DrinkDto getDrinkById(long id);
   DrinkDto createDrink(DrinkDto drinkDto);
   DrinkDto updateDrink(long id, DrinkDto drinkDto);
   void deleteDrink(long id);
}

