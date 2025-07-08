package com.ics.spring_drinks.service;

import com.ics.models.Branch;
import com.ics.models.BranchInventory;
import com.ics.spring_drinks.repository.BranchInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class BranchInventoryService {
    @Autowired
    private BranchInventoryRepository branchInventoryRepository;

    public List<BranchInventory> getBranchInventory(Branch branch) {
        return branchInventoryRepository.findByBranch(branch);
    }

    public List<BranchInventory> getLowStockItems(Branch branch) {
        return branchInventoryRepository.findLowStockItems(branch);
    }

    @Transactional
    public void updateInventory(Branch branch, Long drinkId, int quantity) {
        BranchInventory inventory = branchInventoryRepository.findByBranchAndDrinkId(branch, drinkId);
        if (inventory != null) {
            inventory.setQuantity(quantity);
            inventory.setLastUpdated(Timestamp.from(Instant.now()));
            branchInventoryRepository.save(inventory);
        }
    }

    @Transactional
    public void decrementInventory(Branch branch, Long drinkId, int quantity) {
        BranchInventory inventory = branchInventoryRepository.findByBranchAndDrinkId(branch, drinkId);
        if (inventory != null) {
            inventory.setQuantity(Math.max(0, inventory.getQuantity() - quantity));
            inventory.setLastUpdated(Timestamp.from(Instant.now()));
            branchInventoryRepository.save(inventory);
        }
    }

    public void setLowStockThreshold(Branch branch, Long drinkId, int threshold) {
        BranchInventory inventory = branchInventoryRepository.findByBranchAndDrinkId(branch, drinkId);
        if (inventory != null) {
            inventory.setLowStockThreshold(threshold);
            branchInventoryRepository.save(inventory);
        }
    }
}
