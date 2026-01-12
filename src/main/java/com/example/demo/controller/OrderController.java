package com.example.demo.controller;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.model.OrderInfo;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String listOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("orders", orderService.getUserOrders(userDetails.getUserId()));
        return "orders";
    }
    
  @PostMapping("/orders/cancel/{id}")
  @ResponseBody
  public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
      orderService.cancelOrder(id);
      return ResponseEntity.ok().build();
  }

}