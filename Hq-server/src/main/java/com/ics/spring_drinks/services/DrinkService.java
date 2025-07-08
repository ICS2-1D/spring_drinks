package com.ics.spring_drinks.services;

import com.ics.dtos.DrinkDto;
import com.ics.models.Branch;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DrinkService {
   List<DrinkDto> getAllDrinks(Branch branch);
   DrinkDto getDrinkById(long id, Branch branch);
   DrinkDto createDrink(DrinkDto drinkDto);
   DrinkDto updateDrink(long id, DrinkDto drinkDto, Branch branch);
   void deleteDrink(long id);
   void restockDrink(long drinkId, Branch branch, int quantity);
   List<DrinkDto> getLowStockItems();
}
