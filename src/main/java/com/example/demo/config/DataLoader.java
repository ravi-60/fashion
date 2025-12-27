package com.example.demo.config;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Variant;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VariantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
            ProductRepository productRepository,
            VariantRepository variantRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin")); // password: admin
                admin.setEmail("admin@fashion.com");
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user")); // password: user
                user.setEmail("user@gmail.com");
                user.setRole(User.Role.CUSTOMER);
                userRepository.save(user);
            }

            if (productRepository.count() == 0) {
                // Product 1
                Product p1 = new Product();
                p1.setProductName("Classic T-Shirt");
                p1.setDescription("Cotton casual t-shirt");
                p1.setPrice(new BigDecimal("29.99"));
                p1.setCategory("Men");
                p1.setStatus(Product.Status.AVAILABLE);
                productRepository.save(p1);

                Variant v1 = new Variant();
                v1.setProduct(p1);
                v1.setSize(Variant.Size.M);
                v1.setColor("Blue");
                v1.setStockQuantity(50);
                variantRepository.save(v1);

                Variant v2 = new Variant();
                v2.setProduct(p1);
                v2.setSize(Variant.Size.L);
                v2.setColor("Black");
                v2.setStockQuantity(30);
                variantRepository.save(v2);

                // Product 2
                Product p2 = new Product();
                p2.setProductName("Summer Dress");
                p2.setDescription("Floral print summer dress");
                p2.setPrice(new BigDecimal("49.99"));
                p2.setCategory("Women");
                p2.setStatus(Product.Status.AVAILABLE);
                productRepository.save(p2);

                Variant v3 = new Variant();
                v3.setProduct(p2);
                v3.setSize(Variant.Size.S);
                v3.setColor("Red");
                v3.setStockQuantity(20);
                variantRepository.save(v3);
            }
        };
    }
}
