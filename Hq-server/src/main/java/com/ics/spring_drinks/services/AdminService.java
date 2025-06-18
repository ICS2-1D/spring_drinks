package com.ics.spring_drinks.services;


import com.ics.dtos.RegisterRequest;
import com.ics.models.Admin;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface AdminService {
    void registerAdmin(RegisterRequest registerRequest);
    String login(String username, String password);
    Optional<Admin> getAdminByUsername(String username);
}
