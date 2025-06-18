// src/main/java/com/yourpackage/repository/PaymentRepository.java
package com.ics.repository;

import com.ics.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
