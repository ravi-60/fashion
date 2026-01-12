package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdminService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	public long getTotalUsers() {
		long count = userRepository.count();
		log.info("Admin Dashboard: Fetched total user count: {}", count);
		return count;
	}

	public long getTotalSellers() {
		long count = userRepository.countByRole(User.Role.SELLER);
		log.info("Admin Dashboard: Fetched total seller count: {}", count);
		return count;
	}

	public long getTotalOrders() {
		long count = orderRepository.count();
		log.info("Admin Dashboard: Fetched total orders: {}", count);
		return count;
	}

	public BigDecimal getTotalRevenue() {
		BigDecimal revenue = orderRepository.sumTotalRevenue();
		log.info("Admin Dashboard: Calculated total revenue: {}", revenue);
		return revenue;
	}
}
