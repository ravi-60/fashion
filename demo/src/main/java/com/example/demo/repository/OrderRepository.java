package com.example.demo.repository;

import com.example.demo.model.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderInfo, Long> {
    List<OrderInfo> findByCustomer_UserId(Long userId);
}
