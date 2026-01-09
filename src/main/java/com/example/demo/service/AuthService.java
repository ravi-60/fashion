package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
// ... other imports

import com.example.demo.model.User;
import com.example.demo.model.UserRegistrationDTO; // Import DTO
import com.example.demo.repository.UserRepository;

import jakarta.validation.Valid;

@Service
@Validated
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public void registerUser(@Valid UserRegistrationDTO dto) {
		// Business Logic: Check for duplicate username
		if (userRepository.findFirstByUsername(dto.getUsername()).isPresent()) {
			throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
		}

		// 2. Check Email: Same logic
		if (userRepository.findFirstByEmail(dto.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already taken.");
		}

		// Mapping and Saving
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(dto.getRole()); // Dynamically assigned from dropdown

		userRepository.save(user);
	}

}