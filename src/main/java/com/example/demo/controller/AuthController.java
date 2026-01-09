package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.model.User;
import com.example.demo.model.UserRegistrationDTO;
import com.example.demo.service.AuthService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

	@Autowired
	private AuthService authService;
	
	
	@GetMapping("/")
	public String landing() {
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String showRegisterPage(Model model) {
		model.addAttribute("user", new UserRegistrationDTO());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDto) {
		authService.registerUser(registrationDto);
		return "redirect:/login?success";
	}

}