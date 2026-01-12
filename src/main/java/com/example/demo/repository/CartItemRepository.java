package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomer_UserId(Long userId);
    List<CartItem> findByVariant_VariantIdIn(List<Long> variantIds);

  Optional<CartItem> findByCustomer_UserIdAndVariant_VariantId(Long userId, Long variantId);


}