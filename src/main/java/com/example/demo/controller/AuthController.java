package com.example.demo.controller;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        authService.registerUser(user);
        return "redirect:/login?success";
    }

    // --- API endpoints ---

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<Void> apiRegisterUser(@RequestBody User user) {
        authService.registerUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<String> apiLogin() {
        // Actual authentication handled by Spring Security filter; this endpoint can be used to check if login succeeded
        return ResponseEntity.ok("Login endpoint - rely on Spring Security authentication flow");
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<String> apiLogout() {
        // Logout handled by Spring Security; placeholder to satisfy API contract
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/api/auth/profile")
    @ResponseBody
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = new User();
        user.setUserId(userDetails.getUserId());
        user.setUsername(userDetails.getUsername());
        // Do not expose password
        user.setEmail(null);
        user.setRole(null);
        return ResponseEntity.ok(user);
    }
}