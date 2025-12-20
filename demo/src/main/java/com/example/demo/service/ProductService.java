package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.CartItem;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.Product;
import com.example.demo.model.Variant;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.VariantRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private com.example.demo.repository.CartItemRepository cartItemRepository;

    @Autowired
    private com.example.demo.repository.OrderRepository orderRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
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
        return productRepository.findBySellerId(sellerId);
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
        return orderRepository.findBySellerIdAndStatus(sellerId, OrderInfo.OrderStatus.PLACED);
    }

    public List<OrderInfo> getClosedOrdersForSeller(Long sellerId) {
        return orderRepository.findBySellerIdAndStatusIn(sellerId, List.of(OrderInfo.OrderStatus.DELIVERED, OrderInfo.OrderStatus.CANCELLED));
    }
}