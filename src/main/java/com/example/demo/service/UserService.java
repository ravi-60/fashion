package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public org.springframework.data.domain.Page<User> findUsers(String search, org.springframework.data.domain.Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCase(search, pageable);
        }
        return userRepository.findAll(pageable);
    }
}
