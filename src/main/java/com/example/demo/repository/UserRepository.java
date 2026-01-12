package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String username);
    
    Optional<User> findFirstByUsername(String username);
    Optional<User> findFirstByEmail(String username);
    long countByRole(User.Role role);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
