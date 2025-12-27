package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.ArrayList;
import com.example.demo.model.Variant;
import com.example.demo.config.CustomUserDetails;
import com.example.demo.service.ProductService;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.example.demo.model.Product;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.util.StringUtils;

@Controller
public class SellerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

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
        return "redirect:/seller/manage-orders";
    }

    @GetMapping("/seller/manage-orders")
    public String manageOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("orders", productService.getPlacedOrdersForSeller(userDetails.getUserId()));
        return "seller_manage_orders";
    }

    @GetMapping("/seller/all-orders")
    public String allOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("orders", productService.getClosedOrdersForSeller(userDetails.getUserId()));
        return "seller_all_orders";
    }

    @GetMapping("/seller/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formAction", "/seller/products/add");
        return "admin_product_form";
    }

    @PostMapping("/seller/products/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam Map<String, String> params, @RequestParam(name = "image", required = false) MultipartFile image, @AuthenticationPrincipal com.example.demo.config.CustomUserDetails userDetails) {
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

        // Save image if provided
        if (image != null && !image.isEmpty()) {
            try {
                String savedPath = saveProductImage(product.getProductName(), image);
                product.setImgPath(savedPath);
            } catch (IOException e) {
                // Log error - for now print stacktrace
                e.printStackTrace();
            }
        }

        productService.saveProduct(product);
        return "redirect:/seller/products";
    }

    @PostMapping("/seller/orders/delivered/{id}")
    public String markOrderAsDelivered(@PathVariable Long id, @AuthenticationPrincipal com.example.demo.config.CustomUserDetails userDetails) {
        // Optionally, check if the order belongs to this seller
        orderService.markAsDelivered(id);
        return "redirect:/seller/orders";
    }

    @PostMapping("/seller/orders/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, @AuthenticationPrincipal com.example.demo.config.CustomUserDetails userDetails) {
        // Optionally, check if the order belongs to this seller
        orderService.cancelOrder(id);
        return "redirect:/seller/orders";
    }

    @GetMapping("/seller/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Product product = productService.getProductById(id)
            .filter(p -> p.getSellerId().equals(userDetails.getUserId()))
            .orElseThrow();
        model.addAttribute("product", product);
        model.addAttribute("formAction", "/seller/products/edit/" + id);
        return "admin_product_form";
    }

    @PostMapping("/seller/products/edit/{id}")
    public String editProduct(@PathVariable Long id, @ModelAttribute Product product, @RequestParam(name = "image", required = false) MultipartFile image, @AuthenticationPrincipal CustomUserDetails userDetails) {
        product.setProductId(id);
        product.setSellerId(userDetails.getUserId());

        // Save image if provided
        if (image != null && !image.isEmpty()) {
            try {
                String savedPath = saveProductImage(product.getProductName(), image);
                product.setImgPath(savedPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        productService.saveProduct(product);
        return "redirect:/seller/products";
    }

    @PostMapping("/seller/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Product product = productService.getProductById(id)
            .filter(p -> p.getSellerId().equals(userDetails.getUserId()))
            .orElseThrow();
        productService.deleteProduct(id);
        return "redirect:/seller/products";
    }

    private String saveProductImage(String productName, MultipartFile image) throws IOException {
        // Sanitize product name for filesystem
        String safeName = (productName == null || productName.isBlank()) ? "unnamed" : productName.replaceAll("[^a-zA-Z0-9-_]", "_");
        String filename = StringUtils.cleanPath(image.getOriginalFilename());
        Path uploadDir = Paths.get("src/main/resources/static/images/products/" + safeName);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path target = uploadDir.resolve(filename);
        Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        // Return web-accessible path
        return "/images/products/" + safeName + "/" + filename;
    }
}