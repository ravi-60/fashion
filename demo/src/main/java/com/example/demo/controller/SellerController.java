package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import com.example.demo.model.Variant;
import com.example.demo.config.CustomUserDetails;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.example.demo.model.Product;
import java.util.Map;

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

    @GetMapping("/seller/orders")
    public String listSellerOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("orders", productService.getOrdersForSeller(userDetails.getUserId()));
        return "seller_orders";
    }

    @GetMapping("/seller/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin_product_form";
    }

    @PostMapping("/seller/products/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam Map<String, String> params, @AuthenticationPrincipal com.example.demo.config.CustomUserDetails userDetails) {
        product.setSellerId(userDetails.getUserId());
        // Parse variants from params
        ArrayList<Variant> variants = new ArrayList<>();
        int i = 0;
        while (params.containsKey("variants[" + i + "].color")) {
            Variant variant = new Variant();
            variant.setColor(params.get("variants[" + i + "].color"));
            variant.setSize(Variant.Size.valueOf(params.get("variants[" + i + "].size")));
            variant.setStockQuantity(Integer.parseInt(params.get("variants[" + i + "].stockQuantity")));
            variants.add(variant);
            i++;
        }
        product.setVariants(variants);
        productService.saveProduct(product);
        return "redirect:/seller/products";
    }
}