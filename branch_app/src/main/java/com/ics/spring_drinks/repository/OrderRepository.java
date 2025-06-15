package com.ics.spring_drinks.repository;

import com.ics.models.Branch;
import com.ics.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByBranch(Branch branch);
    Optional<Order> findByOrderId(long orderId);
}
