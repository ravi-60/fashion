package com.example.demo.service;

import com.example.demo.model.CartItem;
import com.example.demo.model.User;
import com.example.demo.model.Variant;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VariantRepository variantRepository;

    public void addToCart(Long userId, Long variantId, int quantity) {
        User user = userRepository.findById(userId).orElseThrow();
        Variant variant = variantRepository.findById(variantId).orElseThrow();

        // Simple implementation: Create new item, could be enhanced to merge quantities
        CartItem item = new CartItem();
        item.setCustomer(user);
        item.setVariant(variant);
        item.setQuantity(quantity);

        cartItemRepository.save(item);
    }

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByCustomer_UserId(userId);
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void clearCart(Long userId) {
        List<CartItem> items = getCartItems(userId);
        cartItemRepository.deleteAll(items);
    }
}