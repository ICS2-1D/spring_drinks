package com.ics.spring_drinks.services;


import com.ics.dtos.CustomerDto;
import com.ics.models.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {
     Customer createCustomer(CustomerDto customerDto);
     Customer getCustomerById(int customerId);
     List<Customer> getAllCustomers();

}
