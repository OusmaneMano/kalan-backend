package com.kalan.controller;

import com.kalan.dto.request.PaymentInitiateRequest;
import com.kalan.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/v1/payments/initiate   body: { courseId, method }
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiate(@RequestBody PaymentInitiateRequest req) {
        return ResponseEntity.ok(paymentService.initiate(req.courseId(), req.method()));
    }

    // POST /api/v1/payments/{id}/confirm
    // Test-mode success (stands in for a provider callback). Grants PAID access.
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.confirm(id));
    }

    // POST /api/v1/payments/webhook/{provider}   (PUBLIC — provider calls this)
    @PostMapping("/webhook/{provider}")
    public ResponseEntity<Map<String, Object>> webhook(
        @PathVariable String provider,
        @RequestBody(required = false) Map<String, Object> payload
    ) {
        return ResponseEntity.ok(
            paymentService.handleWebhook(provider, payload != null ? payload : Map.of()));
    }

    // GET /api/v1/payments   — current user's payment history
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> history() {
        return ResponseEntity.ok(paymentService.history());
    }
}