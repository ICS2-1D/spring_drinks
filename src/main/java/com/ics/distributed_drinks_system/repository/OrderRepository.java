package com.ics.distributed_drinks_system.repository;

import com.ics.distributed_drinks_system.models.Branch;
import com.ics.distributed_drinks_system.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByBranch(Branch branch);

}
