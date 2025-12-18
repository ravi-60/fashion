package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.config.CustomUserDetails;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.example.demo.model.Product;

@Controller
public class SellerController {

    @Autowired
    private ProductService productService;

    @GetMapping("/seller/dashboard")
    public String sellerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        return "seller_dashboard";
    }

    @GetMapping("/seller/products")
    public String listSellerProducts(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, @RequestParam(value = "search", required = false) String search) {
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.getProductsBySellerId(userDetails.getUserId())
                .stream()
                .filter(p -> p.getProductName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        } else {
            products = productService.getProductsBySellerId(userDetails.getUserId());
        }
        model.addAttribute("products", products);
        return "seller_products";
    }
}