package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.Variant;
import com.example.demo.service.ProductService;
import com.example.demo.service.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private VariantService variantService;

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String category, Model model) {
        model.addAttribute("products", productService.filterByCategory(category));
        model.addAttribute("category", category);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(product -> model.addAttribute("product", product));
        return "product_details";
    }

    // --- Required API-style methods (kept separate from view methods) ---

    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        productService.saveProduct(product);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product updated) {
        return productService.getProductById(id)
                .map(existing -> {
                    existing.setProductName(updated.getProductName());
                    existing.setDescription(updated.getDescription());
                    existing.setPrice(updated.getPrice());
                    existing.setCategory(updated.getCategory());
                    existing.setStatus(updated.getStatus());
                    productService.saveProduct(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProductDetails(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // New endpoints: add variant(s) to an existing product without changing DB schema

    // Add a full variant (size + color + optional stockQuantity)
    @PostMapping("/api/products/{productId}/variants")
    @ResponseBody
    public ResponseEntity<?> addVariantToProduct(@PathVariable Long productId, @RequestBody Map<String, Object> body) {
        Optional<Product> opt = productService.getProductById(productId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Product product = opt.get();

        Variant variant = new Variant();
        variant.setProduct(product);

        if (body.containsKey("size") && body.get("size") != null) {
            try {
                variant.setSize(Variant.Size.valueOf(body.get("size").toString()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid size value"));
            }
        }
        if (body.containsKey("color")) {
            variant.setColor(body.get("color") == null ? null : body.get("color").toString());
        }
        if (body.containsKey("stockQuantity")) {
            try {
                variant.setStockQuantity(Integer.parseInt(body.get("stockQuantity").toString()));
            } catch (NumberFormatException e) {
                variant.setStockQuantity(0);
            }
        }

        variantService.saveVariant(variant);
        return ResponseEntity.ok(variant);
    }

    // Add a size-only variant (color left null)
    @PostMapping("/api/products/{productId}/variants/size")
    @ResponseBody
    public ResponseEntity<?> addSizeToProduct(@PathVariable Long productId, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("size") || body.get("size") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing size"));
        }
        String sizeStr = body.get("size").toString();
        Map<String, Object> payload = Map.of("size", sizeStr, "color", null, "stockQuantity", body.getOrDefault("stockQuantity", 0));
        return addVariantToProduct(productId, payload);
    }

    // Add a color-only variant (size left null)
    @PostMapping("/api/products/{productId}/variants/color")
    @ResponseBody
    public ResponseEntity<?> addColorToProduct(@PathVariable Long productId, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("color") || body.get("color") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing color"));
        }
        String color = body.get("color").toString();
        Map<String, Object> payload = Map.of("size", null, "color", color, "stockQuantity", body.getOrDefault("stockQuantity", 0));
        return addVariantToProduct(productId, payload);
    }
}