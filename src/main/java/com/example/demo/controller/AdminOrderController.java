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
    public String listOrders(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                             @RequestParam(name = "sort", required = false, defaultValue = "orderId") String sort,
                             @RequestParam(name = "dir", required = false, defaultValue = "asc") String dir,
                             @RequestParam(name = "sellerId", required = false) Long sellerId,
                             Model model) {
        int size = 5;
        org.springframework.data.domain.Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sort));
        org.springframework.data.domain.Page<OrderInfo> orderPage;
        if (sellerId != null) {
            orderPage = orderService.findOrdersBySellerId(sellerId, pageable);
        } else {
            orderPage = orderService.findOrders(pageable);
        }
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("sellerId", sellerId);
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