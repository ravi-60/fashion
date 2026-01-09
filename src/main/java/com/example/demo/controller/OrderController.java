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

//    // --- API endpoints required by the spec ---
//
//    @PostMapping("/api/orders/place")
//    @ResponseBody
//    public ResponseEntity<Void> placeOrder(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        orderService.placeOrder(userDetails.getUserId());
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/api/orders/{id}")
//    @ResponseBody
//    public ResponseEntity<OrderInfo> getOrderDetails(@PathVariable Long id) {
//        Optional<OrderInfo> order = orderService.getOrderById(id);
//        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    @PostMapping("/api/orders/cancel/{id}")
//    @ResponseBody
//    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
//        orderService.cancelOrder(id);
//        return ResponseEntity.ok().build();
//    }
}