package com.ics.spring_drinks.repository;

import com.ics.models.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {

   Optional<Drink> findById(Long id);
   Optional<Drink> findByDrinkName(String name);
}
