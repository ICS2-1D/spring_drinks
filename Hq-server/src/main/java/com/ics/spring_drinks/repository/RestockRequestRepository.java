package com.ics.spring_drinks.repository;

import com.ics.models.RestockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestockRequestRepository extends JpaRepository<RestockRequest, Long> {

    List<RestockRequest> findByFulfilled(boolean fulfilled);
}