package com.ics.distributed_drinks_system.repository;

import com.ics.distributed_drinks_system.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {


}
