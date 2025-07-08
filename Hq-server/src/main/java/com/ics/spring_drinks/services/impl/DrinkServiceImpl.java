package com.ics.spring_drinks.services.impl;

import com.ics.dtos.DrinkDto;
import com.ics.models.Branch;
import com.ics.models.BranchStock;
import com.ics.models.Drink;
import com.ics.spring_drinks.repository.BranchStockRepository;
import com.ics.spring_drinks.repository.DrinkRepository;
import com.ics.spring_drinks.services.DrinkService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private final DrinkRepository drinkRepository;
    private final BranchStockRepository branchStockRepository;

    @Override
    public List<DrinkDto> getAllDrinks(Branch branch) {
        return branchStockRepository.findByBranch(branch).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DrinkDto getDrinkById(long id, Branch branch) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + id));
        BranchStock branchStock = branchStockRepository.findByBranchAndDrink(branch, drink)
                .orElseThrow(() -> new RuntimeException("Drink not available at this branch"));
        return mapToDto(branchStock);
    }

    @Override
    public DrinkDto createDrink(DrinkDto drinkDto) {
        Drink drink = new Drink();
        drink.setDrinkName(drinkDto.getDrinkName());
        drink.setDrinkPrice(drinkDto.getDrinkPrice());
        Drink savedDrink = drinkRepository.save(drink);

        // Initialize stock for all branches
        for (Branch branch : Branch.values()) {
            BranchStock branchStock = new BranchStock();
            branchStock.setBranch(branch);
            branchStock.setDrink(savedDrink);
            branchStock.setQuantity(drinkDto.getDrinkQuantity()); // Initial quantity
            branchStockRepository.save(branchStock);
        }

        BranchStock initialStock = branchStockRepository.findByBranchAndDrink(Branch.NAIROBI, savedDrink).get();
        return mapToDto(initialStock);
    }

    @Override
    @Transactional
    public DrinkDto updateDrink(long id, DrinkDto drinkDto, Branch branch) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + id));

        BranchStock branchStock = branchStockRepository.findByBranchAndDrink(branch, drink)
                .orElseThrow(() -> new RuntimeException("Drink not available at this branch"));

        if (drinkDto.getDrinkPrice() > 0) {
            drink.setDrinkPrice(drinkDto.getDrinkPrice());
            drinkRepository.save(drink);
        }

        if (drinkDto.getDrinkQuantity() >= 0) {
            branchStock.setQuantity(drinkDto.getDrinkQuantity());
        }

        BranchStock updatedStock = branchStockRepository.save(branchStock);
        return mapToDto(updatedStock);
    }

    @Override
    public void deleteDrink(long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + id));
        drinkRepository.delete(drink);
    }

    @Override
    @Transactional
    public void restockDrink(long drinkId, Branch branch, int quantity) {
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new RuntimeException("Drink not found with id: " + drinkId));
        BranchStock branchStock = branchStockRepository.findByBranchAndDrink(branch, drink)
                .orElseThrow(() -> new RuntimeException("Drink not available at this branch"));

        branchStock.setQuantity(branchStock.getQuantity() + quantity);
        branchStockRepository.save(branchStock);
    }

    /**
     * NEW: Implementation to fetch all low stock items from the repository
     * and map them to DTOs that include branch information.
     */
    @Override
    @Transactional
    public List<DrinkDto> getLowStockItems() {
        return branchStockRepository.findLowStockItems().stream()
                .map(this::mapToDtoWithBranch)
                .collect(Collectors.toList());
    }

    // Mapper for contexts where branch info in the DTO is not needed
    private DrinkDto mapToDto(BranchStock branchStock) {
        Drink drink = branchStock.getDrink();
        return new DrinkDto(
                drink.getId(),
                drink.getDrinkName(),
                branchStock.getQuantity(),
                drink.getDrinkPrice(),
                null // Branch is null
        );
    }

    private DrinkDto mapToDtoWithBranch(BranchStock branchStock) {
        Drink drink = branchStock.getDrink();
        return new DrinkDto(
                drink.getId(),
                drink.getDrinkName(),
                branchStock.getQuantity(),
                drink.getDrinkPrice(),
                branchStock.getBranch() // Set the branch here
        );
    }
}
