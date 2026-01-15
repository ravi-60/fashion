package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;


@Controller
public class ProductController {

    @Autowired
    private ProductService productService;


    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size, // Set to 5 products per page
            @RequestParam(defaultValue = "productId") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            Model model) {

        Sort.Direction direction = 
                dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = 
                PageRequest.of(page, size, Sort.by(direction, sort));

        // Calling the service method
        Page<Product> productPage = 
                productService.getProductsPaged(category, search, pageable);

        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("category", category);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        
        return "products";
    }
    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(product -> model.addAttribute("product", product));
        return "product_details";
    }
}