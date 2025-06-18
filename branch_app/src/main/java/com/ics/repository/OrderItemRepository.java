
// src/main/java/com/yourpackage/repository/OrderItemRepository.java
package com.ics.repository;

import com.ics.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
