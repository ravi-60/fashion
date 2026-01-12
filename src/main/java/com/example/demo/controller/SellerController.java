package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//Spring Data Pagination & Sorting
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.Product;
import com.example.demo.model.Variant;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
@Controller
public class SellerController {

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
		System.out.println("Seller ID: " + sellerId); // Debugging line
		model.addAttribute("username", userDetails.getUsername());
		// product count
		long totalProducts = productService.getTotalProductsForSeller(sellerId);
		model.addAttribute("totalProducts", totalProducts);

		long totalOrdersForSeller = productService.getTotalOrdersForSeller(sellerId);
		model.addAttribute("totalOrders", totalOrdersForSeller);

		model.addAttribute("totalEarnings", productService.getTotalEarningsForSeller(sellerId));

		model.addAttribute("averageOrderValue", productService.getAverageOrderValueForSeller(sellerId));

		return "seller_dashboard";
	}

	
	@GetMapping("/seller/products")
	public String listSellerProducts(
	        @AuthenticationPrincipal CustomUserDetails userDetails, 
	        Model model,
	        @RequestParam(value = "search", required = false) String search,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "productId,asc") String sort) {

	    String[] sortParams = sort.split(",");
	    Sort sortOrder = sortParams[1].equalsIgnoreCase("asc") ? Sort.by(sortParams[0]).ascending() : Sort.by(sortParams[0]).descending();
	    Pageable pageable = PageRequest.of(page, size, sortOrder);

	    // CALL THE NEW METHOD HERE
	    Page<Product> productPage = productService.getProductsBySellerAndSearch(userDetails.getUserId(), search, pageable);

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

	@GetMapping("/seller/orders")
	public String listSellerOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		return "redirect:/seller/manage-orders";
	}

	@GetMapping("/seller/manage-orders")
	public String manageOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
			@RequestParam(value = "search", required = false) String search) {
		List<com.example.demo.model.OrderInfo> orders = productService
				.getPlacedOrdersForSeller(userDetails.getUserId());
		if (search != null && !search.isEmpty()) {
			String s = search.trim();
			orders = orders.stream().filter(o -> o.getOrderId() != null && o.getOrderId().toString().contains(s))
					.toList();
		}
		model.addAttribute("orders", orders);
		model.addAttribute("search", search);
		return "seller_manage_orders";
	}

	
	@GetMapping("/seller/all-orders")
	public String allOrders(
	        @AuthenticationPrincipal CustomUserDetails userDetails, 
	        Model model,
	        @RequestParam(name = "page", defaultValue = "0") int page,
	        @RequestParam(name = "sort", defaultValue = "orderId") String sort,
	        @RequestParam(name = "dir", defaultValue = "desc") String dir,
	        @RequestParam(name = "search", required = false) String search) {

	    int size = 5; // Same page size as your Admin User page
	    org.springframework.data.domain.Sort.Direction direction = 
	        "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC 
	                                   : org.springframework.data.domain.Sort.Direction.ASC;
	    
	    org.springframework.data.domain.Pageable pageable = 
	        org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sort));

	    org.springframework.data.domain.Page<OrderInfo> orderPage = 
	        productService.getClosedOrdersForSellerPaged(userDetails.getUserId(), search, pageable);

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
	public String addProduct(@ModelAttribute Product product, 
	                        @RequestParam("variantsJson") String variantsJson, // Get the JSON string
	                        @RequestParam(name = "image", required = false) MultipartFile image,
	                        @AuthenticationPrincipal CustomUserDetails userDetails) {
	    System.out.println(variantsJson);
	    product.setSellerId(userDetails.getUserId());

	    try {
	        // Parse the JSON string into the List of Variants
	        ObjectMapper mapper = new ObjectMapper();
	        List<Variant> variantList = mapper.readValue(variantsJson, new TypeReference<List<Variant>>(){});
	        
	        // Link variants to product
	        if (variantList != null) {
	            for (Variant variant : variantList) {
	                variant.setProduct(product);
	            }
	            product.setVariants(variantList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace(); // Handle parsing error
	    }

	    // Image logic...
	    if (image != null && !image.isEmpty()) {
	        try {
	            product.setImgPath(saveProductImage(product.getProductName(), image));
	        } catch (IOException e) { e.printStackTrace(); }
	    }

	    productService.saveProduct(product);
	    return "redirect:/seller/products";
	}


	@PostMapping("/seller/orders/delivered/{id}")
	public String markOrderAsDelivered(@PathVariable Long id,
			@AuthenticationPrincipal com.example.demo.config.CustomUserDetails userDetails) {
		// Optionally, check if the order belongs to this seller
		orderService.markAsDelivered(id);
		return "redirect:/seller/orders";
	}

	@PostMapping("/seller/orders/cancel/{id}")
	public String cancelOrder(@PathVariable Long id,
			@AuthenticationPrincipal com.example.demo.config.CustomUserDetails userDetails) {
		// Optionally, check if the order belongs to this seller
		orderService.cancelOrder(id);
		return "redirect:/seller/orders";
	}

	@GetMapping("/seller/products/edit/{id}")
	public String showEditProductForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
			Model model) {
		Product product = productService.getProductById(id).filter(p -> p.getSellerId().equals(userDetails.getUserId()))
				.orElseThrow();
		model.addAttribute("product", product);
		model.addAttribute("formAction", "/seller/products/edit/" + id);
		return "seller_product_form";
	}

	
	@PostMapping("/seller/products/edit/{id}")
	public String editProduct(@PathVariable Long id, 
	                          @ModelAttribute Product product,
	                          @RequestParam("variantsJson") String variantsJson, 
	                          @RequestParam(name = "image", required = false) MultipartFile image,
	                          @AuthenticationPrincipal CustomUserDetails userDetails) {
	    
	    // 1. Load the existing product
	    Product existingProduct = productService.getProductById(id)
	            .filter(p -> p.getSellerId().equals(userDetails.getUserId()))
	            .orElseThrow(() -> new RuntimeException("Product not found"));

	    // 2. Update basic info
	    existingProduct.setProductName(product.getProductName());
	    existingProduct.setDescription(product.getDescription());
	    existingProduct.setCategory(product.getCategory());
	    existingProduct.setPrice(product.getPrice());
	    existingProduct.setStatus(product.getStatus());

	    // 3. SMART VARIANT UPDATE LOGIC
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        List<Variant> incomingVariants = mapper.readValue(variantsJson, new TypeReference<List<Variant>>(){});
	        
	        if (incomingVariants != null) {
	            List<Variant> currentVariants = existingProduct.getVariants();

	            for (Variant incoming : incomingVariants) {
	                // Try to find an existing variant by ID...
	                // OR by matching Size and Color (to prevent duplicates if ID is missing)
	                Variant match = currentVariants.stream()
	                    .filter(v -> (incoming.getVariantId() != null && v.getVariantId().equals(incoming.getVariantId())) 
	                              || (v.getSize() == incoming.getSize() && v.getColor().equalsIgnoreCase(incoming.getColor())))
	                    .findFirst()
	                    .orElse(null);

	                if (match != null) {
	                    // UPDATE existing
	                    match.setStockQuantity(incoming.getStockQuantity());
	                    match.setSize(incoming.getSize());
	                    match.setColor(incoming.getColor());
	                    match.setActive(true);
	                } else {
	                    // ADD NEW only if no match found
	                    incoming.setProduct(existingProduct);
	                    incoming.setActive(true);
	                    currentVariants.add(incoming);
	                }
	            }
	        }
	    } catch (Exception e) { e.printStackTrace(); }

	    // 4. Image logic
	    if (image != null && !image.isEmpty()) {
	        try {
	            existingProduct.setImgPath(saveProductImage(product.getProductName(), image));
	        } catch (IOException e) { e.printStackTrace(); }
	    }

	    productService.saveProduct(existingProduct);
	    return "redirect:/seller/products?updated";
	}

	
	@PostMapping("/seller/products/delete/{id}")
	public String deleteProduct(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
	    Product product = productService.getProductById(id)
	            .filter(p -> p.getSellerId().equals(userDetails.getUserId()))
	            .orElseThrow();
	            
	    // Professional Soft Delete:
	    product.setStatus(Product.Status.OUT_OF_STOCK);
	    product.getVariants().forEach(v -> v.setStockQuantity(0));
	    
	    productService.saveProduct(product);
	    return "redirect:/seller/products?deleted";
	}

	private String saveProductImage(String productName, MultipartFile image) throws IOException {
		String safeName = (productName == null || productName.isBlank()) ? "unnamed"
				: productName.replaceAll("[^a-zA-Z0-9-_]", "_");
		String filename = StringUtils.cleanPath(image.getOriginalFilename());
		Path uploadDir = Paths.get("src/main/resources/static/images/products/" + safeName);
		if (!Files.exists(uploadDir)) {
			Files.createDirectories(uploadDir);
		}
		Path target = uploadDir.resolve(filename);
		Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return "/images/products/" + safeName + "/" + filename;
	}
	
}