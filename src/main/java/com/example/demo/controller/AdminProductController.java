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
import com.example.demo.service.AdminService;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private AdminService adminService;

	@GetMapping
	public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
	                           @RequestParam(name = "sort", defaultValue = "productId") String sort,
	                           @RequestParam(name = "dir", defaultValue = "asc") String dir,
	                           @RequestParam(name = "sellerId", required = false) Long sellerId,
	                           Model model) {
	    
		Page<Product> productPage = adminService.listProductsService(page, sort, dir, sellerId);

	    model.addAttribute("productPage", productPage);
	    model.addAttribute("products", productPage.getContent());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("sort", sort);
	    model.addAttribute("dir", dir);
	    model.addAttribute("sellerId", sellerId); 
	    
	    return "admin_products";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Product product = adminService.showEditFormService(id);
		model.addAttribute("product", product);
		model.addAttribute("formAction", "/admin/products/edit/" + id);
		return "admin_product_form";
	}

	@PostMapping("/edit/{id}")
	public String editProduct(@PathVariable Long id, @ModelAttribute Product product,
			@RequestParam("variantsJson") String variantsJson,
			@RequestParam(name = "image", required = false) MultipartFile image) {

		adminService.editProductService(id, product, variantsJson, image);
		return "redirect:/admin/products?updated";
	}

	@PostMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id) {
		adminService.deleteProductService(id);
		return "redirect:/admin/products?deleted";
	}

}