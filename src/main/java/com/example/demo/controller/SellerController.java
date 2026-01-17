package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.Product;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.SellerService;

@Controller
public class SellerController {

    @Autowired
    private SellerService sellerService; // Use the new Service

    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;

    @GetMapping("/seller/profile")
    public String sellerProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("email", userDetails.getEmail());
        model.addAttribute("role", userDetails.getRole().name());
        return "seller_profile";
    }

    @GetMapping("/seller/dashboard")
    public String sellerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long sellerId = userDetails.getUserId();
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("totalProducts", productService.getTotalProductsForSeller(sellerId));
        model.addAttribute("totalOrders", productService.getTotalOrdersForSeller(sellerId));
        model.addAttribute("totalEarnings", productService.getTotalEarningsForSeller(sellerId));
        model.addAttribute("averageOrderValue", productService.getAverageOrderValueForSeller(sellerId));
        return "seller_dashboard";
    }

    @GetMapping("/seller/products")
    public String listSellerProducts(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "productId,asc") String sort) {

        Page<Product> productPage = sellerService.listSellerProductsService(userDetails.getUserId(), search, page, size, sort);
        String[] sortParams = sort.split(",");

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("sortField", sortParams[0]);
        model.addAttribute("sortDir", sortParams[1]);
        model.addAttribute("reverseSortDir", sortParams[1].equals("asc") ? "desc" : "asc");
        model.addAttribute("search", search);
        return "seller_products";
    }

    @GetMapping("/seller/manage-orders")
    public String manageOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
            @RequestParam(value = "search", required = false) String search) {
        model.addAttribute("orders", sellerService.manageOrdersService(userDetails.getUserId(), search));
        model.addAttribute("search", search);
        return "seller_manage_orders";
    }

    @GetMapping("/seller/all-orders")
    public String allOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "sort", defaultValue = "orderId") String sort,
            @RequestParam(name = "dir", defaultValue = "desc") String dir,
            @RequestParam(name = "search", required = false) String search) {

        Page<OrderInfo> orderPage = sellerService.allOrdersService(userDetails.getUserId(), page, sort, dir, search);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("search", search);
        return "seller_all_orders";
    }
    
	@GetMapping("/seller/products/add")
	public String showAddProductForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("formAction", "/seller/products/add");
		return "seller_product_form";
	}

    @PostMapping("/seller/products/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam("variantsJson") String variantsJson,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sellerService.addProductService(product, variantsJson, image, userDetails.getUserId());
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Product product = sellerService.getProductForEdit(id, userDetails.getUserId());
        model.addAttribute("product", product);
        model.addAttribute("formAction", "/seller/products/edit/" + id);
        return "seller_product_form";
    }

    @PostMapping("/seller/products/edit/{id}")
    public String editProduct(@PathVariable Long id, @ModelAttribute Product product, @RequestParam("variantsJson") String variantsJson,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sellerService.editProductService(id, product, variantsJson, image, userDetails.getUserId());
        return "redirect:/seller/products?updated";
    }

    @PostMapping("/seller/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        sellerService.deleteProductService(id, userDetails.getUserId());
        return "redirect:/seller/products?deleted";
    }
    
    @PostMapping("/seller/orders/delivered/{id}")
    @ResponseBody
	public String markOrderAsDelivered(@PathVariable Long id) {
		// Optionally, check if the order belongs to this seller
		orderService.markAsDelivered(id);
		return "redirect:/seller/orders";
	}
    
    @PostMapping("/seller/orders/cancel/{id}")
    @ResponseBody
	public String cancelOrder(@PathVariable Long id) {
		// Optionally, check if the order belongs to this seller
		orderService.cancelOrder(id);
		return "redirect:/seller/orders";
	}
}