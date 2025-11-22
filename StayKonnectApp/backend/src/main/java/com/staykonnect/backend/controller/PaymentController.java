package com.staykonnect.backend.controller;

import com.staykonnect.backend.entity.Payment;
import com.staykonnect.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(
            @RequestParam Long reservationId,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(paymentService.processPayment(reservationId, transactionId));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<Void> releaseFunds(@PathVariable Long id) {
        paymentService.releaseFunds(id);
        return ResponseEntity.ok().build();
    }
}
