package com.ics.spring_drinks.services.impl;

import com.ics.spring_drinks.dtos.CustomerDto;
import com.ics.spring_drinks.models.Customer;
import com.ics.spring_drinks.repository.CustomerRepository;
import com.ics.spring_drinks.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;


    @Override
    public Customer createCustomer(CustomerDto customerDto) {
       Customer existingCustomer = new Customer();
       existingCustomer.setName(customerDto.getName());
       existingCustomer.setPhoneNumber( customerDto.getPhoneNumber());

        return customerRepository.save(existingCustomer);
    }

    @Override
    public Customer getCustomerById(int customerId) {
       return  customerRepository.findById(customerId).
               orElseThrow(()-> new RuntimeException("Customer not found"));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

}
