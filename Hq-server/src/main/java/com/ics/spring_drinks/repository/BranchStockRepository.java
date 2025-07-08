package com.ics.spring_drinks.repository;

import com.ics.models.Branch;
import com.ics.models.BranchStock;
import com.ics.models.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchStockRepository extends JpaRepository<BranchStock, Long> {

    Optional<BranchStock> findByBranchAndDrink(Branch branch, Drink drink);

    List<BranchStock> findByBranch(Branch branch);

    @Query("SELECT bs FROM BranchStock bs WHERE bs.quantity <= bs.lowStockThreshold")
    List<BranchStock> findLowStockItems();
}
