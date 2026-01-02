package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
	@Autowired
	private ProductService productService;

	@GetMapping
	public String listProducts(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
							   @RequestParam(name = "sort", required = false, defaultValue = "productId") String sort,
							   @RequestParam(name = "dir", required = false, defaultValue = "asc") String dir,
							   @RequestParam(name = "sellerId", required = false) Long sellerId,
							   Model model) {
		// Fixed page size = 5 as requested
		int size = 5;
		org.springframework.data.domain.Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sort));
		org.springframework.data.domain.Page<Product> productPage;
		if (sellerId != null) {
			productPage = productService.findProductsBySellerId(sellerId, pageable);
		} else {
			productPage = productService.findProductsBySearch(null, pageable);
		}

		model.addAttribute("productPage", productPage);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("pageSize", size);
		model.addAttribute("sort", sort);
		model.addAttribute("dir", dir);
		model.addAttribute("sellerId", sellerId);
		return "admin_products";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("formAction", "/admin/products/add");
		return "admin_product_form";
	}

	@PostMapping("/add")
	public String addProduct(@ModelAttribute Product product,
			@RequestParam(name = "image", required = false) MultipartFile image) {
		if (image != null && !image.isEmpty()) {
			try {
				String savedPath = saveProductImage(product.getProductName(), image);
				product.setImgPath(savedPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		productService.saveProduct(product);
		return "redirect:/admin/products";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Product product = productService.getProductById(id).orElseThrow();
		model.addAttribute("product", product);
		model.addAttribute("formAction", "/admin/products/edit/" + id);
		return "admin_product_form";
	}

	@PostMapping("/edit/{id}")
	public String editProduct(@PathVariable Long id, @ModelAttribute Product product,
			@RequestParam(name = "image", required = false) MultipartFile image) {
		product.setProductId(id);

		Product existing = productService.getProductById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

		
		if (image != null && !image.isEmpty()) {
			try {
				String savedPath = saveProductImage(product.getProductName(), image);
				product.setImgPath(savedPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		product.setSellerId(existing.getSellerId());
		product.setImgPath(existing.getImgPath());
		productService.saveProduct(product);
		return "redirect:/admin/products";
	}

	@PostMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return "redirect:/admin/products";
	}

	@GetMapping("/seller/{sellerId}")
	public String listProductsBySeller(@PathVariable Long sellerId, Model model) {
		List<Product> products = productService.getProductsBySellerId(sellerId);
		model.addAttribute("products", products);
		return "admin_products";
	}

	@GetMapping("/search")
	public String searchProducts(@RequestParam String sellerId, Model model) {
		List<Product> products = productService.getProductsBySellerId(Long.valueOf(sellerId));
		model.addAttribute("products", products);
		return "admin_products";
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