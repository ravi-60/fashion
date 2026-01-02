package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findBySellerId(Long sellerId);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);
    long countBySellerId(Long sellerId);

}