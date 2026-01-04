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
        // Determine available stock and existing quantity in cart
        int availableStock = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
        java.util.Optional<CartItem> existing = cartItemRepository.findByCustomer_UserIdAndVariant_VariantId(userId, variantId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int existingQty = item.getQuantity() == null ? 0 : item.getQuantity();
            int desiredTotal = existingQty + quantity;
            // Cap at available stock
            int newQty = Math.min(desiredTotal, availableStock);
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            int toAdd = Math.min(quantity, availableStock);
            CartItem item = new CartItem();
            item.setCustomer(user);
            item.setVariant(variant);
            item.setQuantity(toAdd);
            cartItemRepository.save(item);
        }
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

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        Variant variant = item.getVariant();
        int availableStock = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
        int newQty = Math.min(Math.max(quantity, 0), availableStock);
        if (newQty <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        }
    }
}