package com.example.demo.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.OrderInfo;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Variant;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdminService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
    private OrderService orderService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	public long getTotalUsers() {
		long count = userRepository.count();
		log.info("Admin Dashboard: Fetched total user count: {}", count);
		return count;
	}

	public long getTotalSellers() {
		long count = userRepository.countByRole(User.Role.SELLER);
		log.info("Admin Dashboard: Fetched total seller count: {}", count);
		return count;
	}

	public long getTotalOrders() {
		long count = orderRepository.count();
		log.info("Admin Dashboard: Fetched total orders: {}", count);
		return count;
	}

	public BigDecimal getTotalRevenue() {
		BigDecimal revenue = orderRepository.sumTotalRevenue();
		log.info("Admin Dashboard: Calculated total revenue: {}", revenue);
		return revenue;
	}
	
	
	public Page<OrderInfo> listOrdersService(int page, String sort, String dir, Long sellerId, int size) {
	    Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
	    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

	    if (sellerId != null) {
	        return orderService.findOrdersBySellerId(sellerId, pageable);
	    } else {
	        return orderService.findOrders(pageable);
	    }
	}
	
	public Page<Product> listProductsService(int page, String sort, String dir, Long sellerId) {
	    // Standardizing page size to 5
	    int size = 5;
	    Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
	    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

	    // Decision logic moved here
	    if (sellerId != null) {
	        return productService.findProductsBySellerId(sellerId, pageable);
	    } else {
	        return productService.findProductsBySearch(null, pageable);
	    }
	}
	
	public Product showEditFormService(Long id) {
	    return productService.getProductById(id)
	            .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
	}
	
	public void editProductService(Long id, Product product, String variantsJson, MultipartFile image) {
	    Product existingProduct = productService.getProductById(id)
	            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

	    // Update basic fields
	    existingProduct.setProductName(product.getProductName());
	    existingProduct.setDescription(product.getDescription());
	    existingProduct.setCategory(product.getCategory());
	    existingProduct.setPrice(product.getPrice());

	    // Variant Logic
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        List<Variant> incomingVariants = mapper.readValue(variantsJson, new TypeReference<List<Variant>>() {});
	        if (incomingVariants != null) {
	            int totalStock = 0;
	            for (Variant incoming : incomingVariants) {
	                Variant match = existingProduct.getVariants().stream()
	                    .filter(v -> v.getSize() == incoming.getSize()
	                        && v.getColor().equalsIgnoreCase(incoming.getColor()))
	                    .findFirst().orElse(null);

	                if (match != null) {
	                    match.setStockQuantity(incoming.getStockQuantity());
	                    totalStock += match.getStockQuantity();
	                }
	            }
	            existingProduct.setStatus(totalStock > 0 ? Product.Status.AVAILABLE : Product.Status.OUT_OF_STOCK);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // Image Handling
	    if (image != null && !image.isEmpty()) {
	        try {
	            existingProduct.setImgPath(saveProductImage(product.getProductName(), image));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    productService.saveProduct(existingProduct);
	}
	
	private String saveProductImage(String productName, MultipartFile image) throws IOException {
		String safeName = productName.replaceAll("[^a-zA-Z0-9-_]", "_");
		String filename = StringUtils.cleanPath(image.getOriginalFilename());
		Path uploadDir = Paths.get("src/main/resources/static/images/products/" + safeName);
		if (!Files.exists(uploadDir))
			Files.createDirectories(uploadDir);
		Path target = uploadDir.resolve(filename);
		Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return "/images/products/" + safeName + "/" + filename;
	}
	
	public void deleteProductService(Long id) {
	    Product product = productService.getProductById(id).orElseThrow();

	    // Professional Soft Delete Logic (As per your original code)
	    product.setStatus(Product.Status.OUT_OF_STOCK);
	    if (product.getVariants() != null) {
	        product.getVariants().forEach(v -> v.setStockQuantity(0));
	    }

	    productService.saveProduct(product);
	}

	public Page<User> listUsersService(int page, String sort, String dir, String search, int size) {
	    Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
	    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

	    return userService.findUsers(search, pageable);
	}


	public boolean addAdminService(String username, String email, String password) {
	    // 1. Check if user already exists
	    if (userRepository.findByUsername(username).isPresent()) {
	        return false; 
	    }

	    // 2. Create and set up the new Admin
	    User admin = new User();
	    admin.setUsername(username);
	    admin.setEmail(email);
	    admin.setPassword(passwordEncoder.encode(password)); // Encoding here
	    admin.setRole(User.Role.ADMIN);

	    // 3. Save to database
	    userRepository.save(admin);
	    return true;
	}
}