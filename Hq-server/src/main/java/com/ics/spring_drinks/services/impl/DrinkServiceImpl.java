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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private static final Logger logger = LoggerFactory.getLogger(DrinkServiceImpl.class);
    private final DrinkRepository drinkRepository;
    private final BranchStockRepository branchStockRepository;

    @Override
    public List<DrinkDto> getAllDrinks(Branch branch) {
        logger.info("Fetching all drinks for branch: {}", branch);
        List<BranchStock> stockItems = branchStockRepository.findByBranch(branch);
        logger.info("Found {} stock items for branch {}", stockItems.size(), branch);

        return stockItems.stream()
                .map(this::mapToDto) // Uses the corrected mapper
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
    @Transactional
    public DrinkDto createDrink(DrinkDto drinkDto) {
        logger.info("Creating new drink product: {}", drinkDto.getDrinkName());
        Drink drink = new Drink();
        drink.setDrinkName(drinkDto.getDrinkName());
        drink.setDrinkPrice(drinkDto.getDrinkPrice());
        Drink savedDrink = drinkRepository.save(drink);

        for (Branch branch : Branch.values()) {
            BranchStock branchStock = new BranchStock();
            branchStock.setBranch(branch);
            branchStock.setDrink(savedDrink);
            branchStock.setQuantity(drinkDto.getDrinkQuantity());
            branchStockRepository.save(branchStock);
            logger.info("Initialized stock for {} at {} branch with quantity {}", savedDrink.getDrinkName(), branch, drinkDto.getDrinkQuantity());
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
        logger.info("Restocked {} at {} with {} items. New quantity: {}", drink.getDrinkName(), branch, quantity, branchStock.getQuantity());
    }

    @Override
    @Transactional
    public List<DrinkDto> getLowStockItems() {
        logger.info("Fetching all low stock items from all branches.");
        List<BranchStock> lowStock = branchStockRepository.findLowStockItems();
        logger.info("Found {} low stock items in total.", lowStock.size());
        return lowStock.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private DrinkDto mapToDto(BranchStock branchStock) {
        Drink drink = branchStock.getDrink();
        return new DrinkDto(
                drink.getId(),
                drink.getDrinkName(),
                branchStock.getQuantity(),
                drink.getDrinkPrice(),
                branchStock.getBranch() // This ensures the branch is always included in the DTO
        );
    }
}
