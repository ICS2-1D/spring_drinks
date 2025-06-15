package com.ics.spring_drinks.services;

import com.ics.dtos.DrinkDto;
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

