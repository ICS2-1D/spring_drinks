package com.ics.spring_drinks.services.impl;

import com.ics.dtos.RegisterRequest;
import com.ics.models.Admin;
import com.ics.spring_drinks.repository.AdminRepository;
import com.ics.spring_drinks.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    @Override
    public void registerAdmin(RegisterRequest registerRequest) {
        // Check if the admin already exists
        if (adminRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Admin with this username already exists");
        }

        // Create a new admin entity
        Admin admin = new Admin();
        admin.setUsername(registerRequest.getUsername());
        admin.setPassword(hashPassword(registerRequest.getPassword()));

        // Save the new admin to the repository
        adminRepository.save(admin);

    }

    @Override
    public String login(String username, String password) {
       Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Check if the password matches
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // Update last login time
        admin.setLast_Login(LocalDateTime.now());
        adminRepository.save(admin);

        return "Login successful";
    }

    @Override
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username)
                .or(() -> {
                    throw new IllegalArgumentException("Admin not found with username: " + username);
                });
    }
}
