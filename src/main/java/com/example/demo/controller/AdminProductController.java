package com.example.demo.controller;

import java.io.IOException;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Product;
import com.example.demo.model.Variant;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

	@Autowired
	private ProductService productService;

	@GetMapping
	public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
	                           @RequestParam(name = "sort", defaultValue = "productId") String sort,
	                           @RequestParam(name = "dir", defaultValue = "asc") String dir,
	                           @RequestParam(name = "sellerId", required = false) Long sellerId,
	                           Model model) {
	    
	    // 1. Setup Pagination (Page size 5 as per your previous requirement)
	    int size = 5;
	    Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
	    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
	    
	    Page<Product> productPage;

	    // 2. The Search Logic
	    // If sellerId is provided via the search input, filter by it. 
	    // Otherwise, fetch all products.
	    if (sellerId != null) {
	        productPage = productService.findProductsBySellerId(sellerId, pageable);
	    } else {
	        productPage = productService.findProductsBySearch(null, pageable);
	    }

	    // 3. Add attributes to model to maintain state in the UI
	    model.addAttribute("productPage", productPage);
	    model.addAttribute("products", productPage.getContent());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("sort", sort);
	    model.addAttribute("dir", dir);
	    model.addAttribute("sellerId", sellerId); // This keeps the ID in the search box after clicking search
	    
	    return "admin_products";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Product product = productService.getProductById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
		model.addAttribute("product", product);
		model.addAttribute("formAction", "/admin/products/edit/" + id);
		return "admin_product_form";
	}

	@PostMapping("/edit/{id}")
	public String editProduct(@PathVariable Long id, @ModelAttribute Product product,
			@RequestParam("variantsJson") String variantsJson,
			@RequestParam(name = "image", required = false) MultipartFile image) {

		Product existingProduct = productService.getProductById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		// Update basic fields
		existingProduct.setProductName(product.getProductName());
		existingProduct.setDescription(product.getDescription());
		existingProduct.setCategory(product.getCategory());
		existingProduct.setPrice(product.getPrice());

		// Variant Logic: Syncing stock and auto-updating status
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<Variant> incomingVariants = mapper.readValue(variantsJson, new TypeReference<List<Variant>>() {
			});
			if (incomingVariants != null) {
				int totalStock = 0;
				for (Variant incoming : incomingVariants) {
					Variant match = existingProduct.getVariants().stream().filter(v -> v.getSize() == incoming.getSize()
							&& v.getColor().equalsIgnoreCase(incoming.getColor())).findFirst().orElse(null);

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

		if (image != null && !image.isEmpty()) {
			try {
				existingProduct.setImgPath(saveProductImage(product.getProductName(), image));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		productService.saveProduct(existingProduct);
		return "redirect:/admin/products?updated";
	}

	@PostMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id) {
		Product product = productService.getProductById(id).orElseThrow();

		// Professional Soft Delete
		product.setStatus(Product.Status.OUT_OF_STOCK);
		if (product.getVariants() != null) {
			product.getVariants().forEach(v -> v.setStockQuantity(0));
		}

		productService.saveProduct(product);
		return "redirect:/admin/products?deleted";
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
		String safeName = productName.replaceAll("[^a-zA-Z0-9-_]", "_");
		String filename = StringUtils.cleanPath(image.getOriginalFilename());
		Path uploadDir = Paths.get("src/main/resources/static/images/products/" + safeName);
		if (!Files.exists(uploadDir))
			Files.createDirectories(uploadDir);
		Path target = uploadDir.resolve(filename);
		Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return "/images/products/" + safeName + "/" + filename;
	}
}