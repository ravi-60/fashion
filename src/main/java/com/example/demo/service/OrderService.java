package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.CartItem;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Payment;
import com.example.demo.model.User;
import com.example.demo.model.Variant;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VariantRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private VariantRepository variantRepository;

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
            .collect(Collectors.groupingBy(item -> item.getVariant().getProduct().getSellerId()));

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

            // Save order items and adjust stock immediately (stock will be decreased when order placed)
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : sellerItems) {
                OrderItem oi = new OrderItem();
                oi.setOrder(savedOrder);
                oi.setVariant(item.getVariant());
                oi.setQuantity(item.getQuantity());
                oi.setUnitPrice(item.getVariant().getProduct().getPrice());
                orderItemRepository.save(oi);
                orderItems.add(oi);

                // Decrease variant stock
                Variant v = item.getVariant();
                Integer current = v.getStockQuantity() == null ? 0 : v.getStockQuantity();
                v.setStockQuantity(current - (item.getQuantity() == null ? 0 : item.getQuantity()));
                variantRepository.save(v);
            }
            savedOrder.setOrderItems(orderItems);
            orderRepository.save(savedOrder);

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

    @Transactional
    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getStatus() != OrderInfo.OrderStatus.DELIVERED) {
                // restore stock for each order item
                if (order.getOrderItems() != null) {
                    for (OrderItem oi : order.getOrderItems()) {
                        Variant v = oi.getVariant();
                        Integer current = v.getStockQuantity() == null ? 0 : v.getStockQuantity();
                        v.setStockQuantity(current + (oi.getQuantity() == null ? 0 : oi.getQuantity()));
                        variantRepository.save(v);
                    }
                }
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

    public Page<OrderInfo> findOrdersBySellerId(Long sellerId, Pageable pageable) {
        return orderRepository.findBySellerId(sellerId, pageable);
    }

    public Page<OrderInfo> findOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}