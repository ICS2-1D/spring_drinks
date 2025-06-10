package com.ics.distributed_drinks_system.repository;

import com.ics.distributed_drinks_system.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
