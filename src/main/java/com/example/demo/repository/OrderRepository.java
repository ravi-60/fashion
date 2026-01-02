package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.OrderInfo;

public interface OrderRepository extends JpaRepository<OrderInfo, Long> {
    List<OrderInfo> findByCustomer_UserId(Long userId);
    List<OrderInfo> findBySellerId(Long sellerId);
    List<OrderInfo> findBySellerIdAndStatus(Long sellerId, OrderInfo.OrderStatus status);
    List<OrderInfo> findBySellerIdAndStatusIn(Long sellerId, List<OrderInfo.OrderStatus> statuses);
    long countBySellerId(Long sellerId);
    
    @Query("""
    		SELECT COALESCE(SUM(o.totalAmount), 0)
    		FROM OrderInfo o
    		WHERE o.sellerId = :sellerId
    		AND o.status <> 'CANCELLED'
    		""")
    		BigDecimal sumTotalAmountBySeller(@Param("sellerId") Long sellerId);
    
    @Query("""
    		SELECT COALESCE(AVG(o.totalAmount), 0)
    		FROM OrderInfo o
    		WHERE o.sellerId = :sellerId
    		AND o.status <> 'CANCELLED'
    		""")
    		BigDecimal findAverageOrderValueBySeller(@Param("sellerId") Long sellerId);
    
    @Query("""
    		SELECT COALESCE(SUM(o.totalAmount), 0)
    		FROM OrderInfo o
    		WHERE o.status <> 'CANCELLED'
    		""")
    		BigDecimal sumTotalRevenue();

    org.springframework.data.domain.Page<OrderInfo> findAll(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<OrderInfo> findBySellerId(Long sellerId, org.springframework.data.domain.Pageable pageable);


}