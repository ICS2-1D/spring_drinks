// src/main/java/com/yourpackage/repository/OrderRepository.java
package com.ics.repository;

import com.ics.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}