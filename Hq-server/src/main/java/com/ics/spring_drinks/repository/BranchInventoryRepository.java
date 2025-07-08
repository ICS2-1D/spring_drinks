package com.ics.spring_drinks.repository;

import com.ics.models.Branch;
import com.ics.models.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {
    List<BranchInventory> findByBranch(Branch branch);

    @Query("SELECT bi FROM BranchInventory bi WHERE bi.branch = :branch AND bi.quantity <= bi.lowStockThreshold")
    List<BranchInventory> findLowStockItems(@Param("branch") Branch branch);

    BranchInventory findByBranchAndDrinkId(Branch branch, Long drinkId);
}
