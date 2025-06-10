package com.ics.distributed_drinks_system.repository;

import com.ics.distributed_drinks_system.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findById(long integer);
}
