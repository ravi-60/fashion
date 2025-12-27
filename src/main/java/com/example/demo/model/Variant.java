package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Variant")
public class Variant implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variantId")
    private Long variantId;

    @ManyToOne
    @JoinColumn(name = "productId")
    @JsonBackReference
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "size")
    private Size size;

    @Column(name = "color")
    private String color;

    @Column(name = "stockQuantity")
    private Integer stockQuantity;

    public enum Size {
        S, M, L, XL
    }

    public Variant() {}

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}