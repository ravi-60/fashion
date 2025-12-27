package com.example.demo.controller;

import com.example.demo.model.Variant;
import com.example.demo.service.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/variant")
public class VariantController {

    @Autowired
    private VariantService variantService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Variant>> getVariantsByProduct(@PathVariable Long productId) {
        List<Variant> variants = variantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(variants);
    }

    @PostMapping("/add")
    public ResponseEntity<Variant> addVariant(@RequestBody Variant variant) {
        variantService.saveVariant(variant);
        return ResponseEntity.ok(variant);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Variant> updateVariant(@PathVariable Long id, @RequestBody Variant updated) {
        Variant existing = variantService.getVariantById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setSize(updated.getSize());
        existing.setColor(updated.getColor());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setProduct(updated.getProduct());
        variantService.saveVariant(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }
}
