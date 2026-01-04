package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderInfo order;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private Variant variant;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    public OrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrderInfo getOrder() { return order; }
    public void setOrder(OrderInfo order) { this.order = order; }

    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
