package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomer_UserId(Long userId);
    List<CartItem> findByVariant_VariantIdIn(List<Long> variantIds);

    // Find existing cart item for a given user and variant (to merge quantities)
    java.util.Optional<CartItem> findByCustomer_UserIdAndVariant_VariantId(Long userId, Long variantId);


}