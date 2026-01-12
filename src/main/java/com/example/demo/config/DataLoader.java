package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Configuration
public class DataLoader {

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.count() == 0) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin"));
				admin.setEmail("admin@fashion.com");
				admin.setRole(User.Role.ADMIN);
				userRepository.save(admin);

				User user = new User();
				user.setUsername("user");
				user.setPassword(passwordEncoder.encode("user"));
				user.setEmail("user@gmail.com");
				user.setRole(User.Role.CUSTOMER);
				userRepository.save(user);
			}

		};
	}
}
