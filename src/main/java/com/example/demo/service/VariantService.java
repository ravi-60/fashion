package com.example.demo.service;

import com.example.demo.model.Variant;
import com.example.demo.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VariantService {

    @Autowired
    private VariantRepository variantRepository;

    public List<Variant> getVariantsByProductId(Long productId) {
        return variantRepository.findByProduct_ProductId(productId);
    }

    public void saveVariant(Variant variant) {
        variantRepository.save(variant);
    }

    public void deleteVariant(Long id) {
        variantRepository.deleteById(id);
    }

    public Variant getVariantById(Long id) {
        return variantRepository.findById(id).orElse(null);
    }
}
