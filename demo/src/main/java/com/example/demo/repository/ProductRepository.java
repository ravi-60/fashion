package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findBySellerId(Long sellerId);

    @Query("SELECT new map(o.orderId as orderId, p.productName as productName, o.orderDate as orderDate, o.status as status, o.totalAmount as totalAmount, u.username as customerName) " +
           "FROM OrderInfo o JOIN CartItem ci ON ci.customer.userId = o.customer.userId " +
           "JOIN Variant v ON ci.variant.variantId = v.variantId " +
           "JOIN Product p ON v.product.productId = p.productId " +
           "JOIN User u ON o.customer.userId = u.userId " +
           "WHERE p.sellerId = :sellerId")
    List<Map<String, Object>> findOrdersForSeller(@Param("sellerId") Long sellerId);
}