package com.ics.repository;

import com.ics.model.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DrinkRepository extends JpaRepository<Drink, Long> {
    Optional<Drink> findByDrinkName(String drinkName);
}
