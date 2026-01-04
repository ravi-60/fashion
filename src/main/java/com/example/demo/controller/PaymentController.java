package com.example.demo.controller;

import com.example.demo.model.Payment;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/make")
    public ResponseEntity<Payment> makePayment(@RequestBody Payment payment) {
        Payment saved = paymentService.makePayment(payment);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Long orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentDetails(@PathVariable Long paymentId) {
        Optional<Payment> paymentOpt = paymentService.getPaymentDetails(paymentId);
        return paymentOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
