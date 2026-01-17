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

	public void registerUser(UserRegistrationDTO dto) {

		if (userRepository.findFirstByUsername(dto.getUsername()).isPresent()) {
			throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
		}

		if (userRepository.findFirstByEmail(dto.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already taken.");
		}
		
		if (dto.getRole().name().equals("ADMIN")) {
			throw new IllegalArgumentException("You can't create a ADMIN");
		}

		User user = new User();
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(dto.getRole());

		userRepository.save(user);
	}

}