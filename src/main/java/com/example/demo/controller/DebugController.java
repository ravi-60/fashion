package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/schema/users")
    public ResponseEntity<List<Map<String, Object>>> userTableColumns() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND (TABLE_NAME = 'User' OR TABLE_NAME = 'user')";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/raw-users")
    public ResponseEntity<List<Map<String, Object>>> rawUsers() {
        String sql = "SELECT username, password, role FROM `User`";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/test-login")
    public ResponseEntity<?> testLogin(@RequestParam String username, @RequestParam String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return ResponseEntity.ok(Map.of("status", "OK", "username", username));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("status", "FAIL", "reason", "Bad credentials"));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(500).body(Map.of("status", "ERROR", "reason", ex.getMessage()));
        }
    }

    @GetMapping("/schema/{table}")
    public ResponseEntity<List<Map<String, Object>>> tableColumns(@PathVariable String table) {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "'";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/raw-payments")
    public ResponseEntity<List<Map<String, Object>>> rawPayments() {
        String sql = "SELECT payment_id, order_id, payment_amount, payment_date, payment_status FROM `Payment`";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/raw-orders")
    public ResponseEntity<List<Map<String, Object>>> rawOrders() {
        String sql = "SELECT orderId, customerId, orderDate, totalAmount, status FROM `OrderInfo`";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(rows);
    }
}