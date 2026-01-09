package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.model.UserRegistrationDTO;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationErrors(MethodArgumentNotValidException ex, Model model) {
        model.addAttribute("org.springframework.validation.BindingResult.user", ex.getBindingResult());
        model.addAttribute("user", ex.getBindingResult().getTarget());
        return "register"; 
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleLogicErrors(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("user", new UserRegistrationDTO());
        return "register";
    }
}