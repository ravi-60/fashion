package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.model.CartItem;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.Product;
import com.example.demo.model.Variant;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.VariantRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> findProductsBySellerId(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }

    public Page<Product> findProductsBySearch(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return productRepository.findByProductNameContainingIgnoreCase(search, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void saveProduct(Product product) {
        if (product.getVariants() != null) {
            for (Variant variant : product.getVariants()) {
                variant.setProduct(product);
            }
        }
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> filterByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsBySellerId(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        // Ensure variants are initialized to avoid lazy-loading issues in templates
        products.forEach(p -> {
            if (p.getVariants() != null) {
                p.getVariants().size();
            }
        });
        return products;
    }

    public List<CartItem> getCartItemsForSeller(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        if (products.isEmpty()) return List.of();
        List<Long> productIds = products.stream().map(Product::getProductId).toList();
        List<Variant> variants = variantRepository.findByProduct_ProductIdIn(productIds);
        if (variants.isEmpty()) return List.of();
        List<Long> variantIds = variants.stream().map(v -> v.getVariantId()).toList();
        return cartItemRepository.findByVariant_VariantIdIn(variantIds);
    }

    public List<OrderInfo> getOrdersForSellerBySellerId(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public List<OrderInfo> getPlacedOrdersForSeller(Long sellerId) {
        List<OrderInfo> orders = orderRepository.findBySellerIdAndStatus(sellerId, OrderInfo.OrderStatus.PLACED);
        // initialize order items and their variants/products to avoid lazy-loading in templates
        orders.forEach(o -> {
            if (o.getOrderItems() != null) {
                o.getOrderItems().forEach(oi -> {
                    if (oi.getVariant() != null) {
                        // touch variant fields
                        oi.getVariant().getVariantId();
                        if (oi.getVariant().getProduct() != null) {
                            oi.getVariant().getProduct().getProductName();
                        }
                    }
                });
            }
        });
        return orders;
    }

    
    
    public Page<OrderInfo> getClosedOrdersForSellerPaged(
            Long sellerId, String search, Pageable pageable) {
        
        java.util.List<OrderInfo.OrderStatus> closedStatuses = java.util.List.of(
            OrderInfo.OrderStatus.DELIVERED, 
            OrderInfo.OrderStatus.CANCELLED
        );
        
        Page<OrderInfo> orderPage = 
            orderRepository.findClosedOrdersBySeller(sellerId, closedStatuses, search, pageable);
        
        // Pre-initialize lazy collections for the template
        orderPage.getContent().forEach(o -> {
            if (o.getCustomer() != null) o.getCustomer().getUsername();
            o.getOrderItems().forEach(oi -> {
                if (oi.getVariant() != null && oi.getVariant().getProduct() != null) {
                    oi.getVariant().getProduct().getProductName();
                }
            });
        });
        
        return orderPage;
    }
    public long getTotalProductsForSeller(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }
    
    public long getTotalOrdersForSeller(Long sellerId) {
        return orderRepository.countBySellerId(sellerId);
    }
    
    public BigDecimal getTotalEarningsForSeller(Long sellerId) {
        return orderRepository.sumTotalAmountBySeller(sellerId);
    }
    
    public BigDecimal getAverageOrderValueForSeller(Long sellerId) {
        return orderRepository.findAverageOrderValueBySeller(sellerId);
    }

    public Page<Product> getProductsPaged(
            String category, String search, Pageable pageable) {
        
        // Search by name takes priority
        if (search != null && !search.isEmpty()) {
            return productRepository.findByProductNameContainingIgnoreCase(search, pageable);
        }
        
        // Filter by category if search is empty
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category, pageable);
        }
        
        // Default view (All products)
        return productRepository.findAll(pageable);
    }
    
    public Page<Product> getProductsBySellerAndSearch(Long sellerId, String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return productRepository.findBySellerIdAndProductNameContainingIgnoreCase(sellerId, search, pageable);
        }
        return productRepository.findBySellerId(sellerId, pageable);
    }

}