package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(User user) {
        System.out.println("Registering user: " + user.getUsername());
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Default to CUSTOMER if not set
        if (user.getRole() == null) {
            user.setRole(User.Role.CUSTOMER);
        }
        System.out.println("Encoded password for " + user.getUsername() + ": " + user.getPassword());
        userRepository.save(user);
    }
}