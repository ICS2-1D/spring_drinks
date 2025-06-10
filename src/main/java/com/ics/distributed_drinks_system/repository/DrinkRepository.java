package com.ics.distributed_drinks_system.repository;

import com.ics.distributed_drinks_system.models.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {
    @Override
    Optional<Drink> findById(Long integer);
}
