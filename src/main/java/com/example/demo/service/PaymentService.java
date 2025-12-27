package com.example.demo.service;

import com.example.demo.model.Payment;
import com.example.demo.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment makePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrder_OrderId(orderId);
    }

    public Optional<Payment> getPaymentDetails(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }
}
