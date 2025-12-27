package com.example.demo.controller;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.model.CartItem;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems(userDetails.getUserId()));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long variantId,
            @RequestParam int quantity) {
        cartService.addToCart(userDetails.getUserId(), variantId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        orderService.placeOrder(userDetails.getUserId());
        return "redirect:/orders";
    }

    // --- API endpoints ---

    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<List<CartItem>> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCartItems(userDetails.getUserId()));
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Void> apiAddToCart(@RequestParam Long userId, @RequestParam Long variantId, @RequestParam int quantity) {
        cartService.addToCart(userId, variantId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/remove/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Void> apiRemoveFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok().build();
    }
}