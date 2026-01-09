package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
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

    Page<OrderInfo> findAll(org.springframework.data.domain.Pageable pageable);
    Page<OrderInfo> findBySellerId(Long sellerId, org.springframework.data.domain.Pageable pageable);

    
    // edited
 // Add this to your OrderRepository
    @Query("SELECT o FROM OrderInfo o WHERE o.sellerId = :sellerId " +
           "AND o.status IN :statuses " +
           "AND (:search IS NULL OR CAST(o.orderId AS string) LIKE %:search%)")
    org.springframework.data.domain.Page<OrderInfo> findClosedOrdersBySeller(
        @Param("sellerId") Long sellerId, 
        @Param("statuses") java.util.List<OrderInfo.OrderStatus> statuses, 
        @Param("search") String search, 
        org.springframework.data.domain.Pageable pageable
    );

}