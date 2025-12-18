package com.example.demo.repository;

import com.example.demo.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findByProduct_ProductId(Long productId);
}
