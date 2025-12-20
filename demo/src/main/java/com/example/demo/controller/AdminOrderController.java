package com.example.demo.controller;

import com.example.demo.model.OrderInfo;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(Model model) {
        List<OrderInfo> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin_orders";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "redirect:/admin/orders";
    }

    @PostMapping("/delivered/{id}")
    public String markAsDelivered(@PathVariable Long id) {
        orderService.markAsDelivered(id);
        return "redirect:/admin/orders";
    }
}