package com.example.demo.service;

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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.OrderInfo;
import com.example.demo.model.Product;
import com.example.demo.model.Variant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SellerService {

    @Autowired
    private ProductService productService;

    // --- Product Listing Logic ---
    public Page<Product> listSellerProductsService(Long sellerId, String search, int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.by(sortParams[0]).ascending() 
            : Sort.by(sortParams[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return productService.getProductsBySellerAndSearch(sellerId, search, pageable);
    }

    // --- Order Logic ---
    public List<OrderInfo> manageOrdersService(Long sellerId, String search) {
        List<OrderInfo> orders = productService.getPlacedOrdersForSeller(sellerId);
        if (search != null && !search.isEmpty()) {
            String s = search.trim();
            orders = orders.stream()
                .filter(o -> o.getOrderId() != null && o.getOrderId().toString().contains(s))
                .toList();
        }
        return orders;
    }

    public Page<OrderInfo> allOrdersService(Long sellerId, int page, String sort, String dir, String search) {
        int size = 5;
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        return productService.getClosedOrdersForSellerPaged(sellerId, search, pageable);
    }

    // --- Add Product Logic ---
    public void addProductService(Product product, String variantsJson, MultipartFile image, Long sellerId) {
        product.setSellerId(sellerId);

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Variant> variantList = mapper.readValue(variantsJson, new TypeReference<List<Variant>>(){});
            if (variantList != null) {
                for (Variant variant : variantList) {
                    variant.setProduct(product);
                }
                product.setVariants(variantList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (image != null && !image.isEmpty()) {
            try {
                product.setImgPath(saveProductImage(product.getProductName(), image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.saveProduct(product);
    }

    // --- Edit Product Logic ---
    public Product getProductForEdit(Long id, Long sellerId) {
        return productService.getProductById(id)
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow();
    }

    public void editProductService(Long id, Product product, String variantsJson, MultipartFile image, Long sellerId) {
        Product existingProduct = productService.getProductById(id)
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existingProduct.setProductName(product.getProductName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStatus(product.getStatus());

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Variant> incomingVariants = mapper.readValue(variantsJson, new TypeReference<List<Variant>>(){});
            if (incomingVariants != null) {
                List<Variant> currentVariants = existingProduct.getVariants();
                for (Variant incoming : incomingVariants) {
                    Variant match = currentVariants.stream()
                        .filter(v -> (incoming.getVariantId() != null && v.getVariantId().equals(incoming.getVariantId())) 
                                  || (v.getSize() == incoming.getSize() && v.getColor().equalsIgnoreCase(incoming.getColor())))
                        .findFirst().orElse(null);

                    if (match != null) {
                        match.setStockQuantity(incoming.getStockQuantity());
                        match.setSize(incoming.getSize());
                        match.setColor(incoming.getColor());
                        match.setActive(true);
                    } else {
                        incoming.setProduct(existingProduct);
                        incoming.setActive(true);
                        currentVariants.add(incoming);
                    }
                }
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
    }

    // --- Delete Logic ---
    public void deleteProductService(Long id, Long sellerId) {
        Product product = productService.getProductById(id)
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow();
        product.setStatus(Product.Status.OUT_OF_STOCK);
        product.getVariants().forEach(v -> v.setStockQuantity(0));
        productService.saveProduct(product);
    }

    // --- Helper for Images ---
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