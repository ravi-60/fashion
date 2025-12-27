package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void placeOrder(Long userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId).orElseThrow();

        // Group cart items by sellerId
        Map<Long, List<CartItem>> itemsBySeller = cartItems.stream()
            .collect(java.util.stream.Collectors.groupingBy(item -> item.getVariant().getProduct().getSellerId()));

        for (var entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItem> sellerItems = entry.getValue();
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : sellerItems) {
                BigDecimal price = item.getVariant().getProduct().getPrice();
                total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            OrderInfo order = new OrderInfo();
            order.setCustomer(user);
            order.setOrderDate(LocalDate.now());
            order.setTotalAmount(total);
            order.setStatus(OrderInfo.OrderStatus.PLACED);
            order.setSellerId(sellerId);
            OrderInfo savedOrder = orderRepository.save(order);

            // Mock Payment
            Payment payment = new Payment();
            payment.setOrder(savedOrder);
            payment.setPaymentAmount(total);
            payment.setPaymentDate(LocalDate.now());
            payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        }
        cartService.clearCart(userId);
    }

    public List<OrderInfo> getUserOrders(Long userId) {
        return orderRepository.findByCustomer_UserId(userId);
    }

    public Optional<OrderInfo> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getStatus() != OrderInfo.OrderStatus.DELIVERED) {
                order.setStatus(OrderInfo.OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        });
    }

    public void markAsDelivered(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getStatus() != OrderInfo.OrderStatus.CANCELLED) {
                order.setStatus(OrderInfo.OrderStatus.DELIVERED);
                orderRepository.save(order);
            }
        });
    }

    public List<OrderInfo> getAllOrders() {
        return orderRepository.findAll();
    }
}