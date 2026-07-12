package com.kalan.service;

import com.kalan.entity.Course;
import com.kalan.entity.Enrollment;
import com.kalan.entity.Payment;
import com.kalan.entity.User;
import com.kalan.repository.CourseRepository;
import com.kalan.repository.EnrollmentRepository;
import com.kalan.repository.PaymentRepository;
import com.kalan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    // ── 1) Start a payment ────────────────────────────────────────────────────
    // Creates a PENDING transaction and returns what the app needs to proceed.
    // For a real provider this is where we'd call their API to get a checkout URL
    // (card / PayPal) or push a Mobile Money prompt, then return that here.
    public Map<String, Object> initiate(Long courseId, String method) {
        User user = getCurrentUser();
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.isFree()) {
            throw new IllegalStateException("This course is free — no payment needed.");
        }

        double amount = course.getPriceXof() != null ? course.getPriceXof() : 5000;
        String currency = "XOF";
        String provider = pickProvider(method);           // MOCK for now
        String reference = "KLN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
            .user(user)
            .course(course)
            .amount(amount)
            .currency(currency)
            .method(method)
            .provider(provider)
            .providerRef(reference)
            .status(Payment.Status.PENDING)
            .build();
        paymentRepository.save(payment);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentId", payment.getId());
        body.put("reference", reference);
        body.put("provider",  provider);
        body.put("method",    method);
        body.put("amount",    amount);
        body.put("currency",  currency);
        // For real providers, these get filled in:
        body.put("checkoutUrl", null);   // card / PayPal redirect URL
        body.put("status", payment.getStatus().name());
        return body;
    }

    // ── 2) Confirm a payment (MOCK success) ────────────────────────────────────
    // Stands in for a real provider callback while payments are in test mode.
    // Marks the transaction SUCCESS and grants PAID access to the course.
    public Map<String, Object> confirm(Long paymentId) {
        User user = getCurrentUser();
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Not your payment.");
        }

        payment.setStatus(Payment.Status.SUCCESS);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        grantAccess(payment);

        Map<String, Object> body = new HashMap<>();
        body.put("status", "SUCCESS");
        body.put("courseId", payment.getCourse().getId());
        body.put("access", true);
        body.put("paymentStatus", Enrollment.PaymentStatus.PAID.name());
        return body;
    }

    // ── 3) Provider webhook (STUB) ─────────────────────────────────────────────
    // Real providers POST here when a payment settles. Left open in security
    // config. TODO: verify the provider signature before trusting the payload.
    public Map<String, Object> handleWebhook(String provider, Map<String, Object> payload) {
        String reference = String.valueOf(payload.getOrDefault("reference", ""));
        String status    = String.valueOf(payload.getOrDefault("status", "")).toUpperCase();

        Payment payment = paymentRepository.findByProviderRef(reference).orElse(null);
        if (payment == null) {
            return Map.of("handled", false, "reason", "unknown reference");
        }

        if (status.equals("SUCCESS") || status.equals("PAID") || status.equals("COMPLETED")) {
            payment.setStatus(Payment.Status.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            grantAccess(payment);
            return Map.of("handled", true, "access", true);
        } else if (status.equals("FAILED") || status.equals("CANCELLED")) {
            payment.setStatus(Payment.Status.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
        return Map.of("handled", true, "access", false);
    }

    // ── 4) Payment history for the current user ───────────────────────────────
    public List<Map<String, Object>> history() {
        User user = getCurrentUser();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("courseId", p.getCourse().getId());
                m.put("courseTitle", p.getCourse().getTitleFr());
                m.put("amount", p.getAmount());
                m.put("currency", p.getCurrency());
                m.put("method", p.getMethod());
                m.put("status", p.getStatus().name());
                m.put("createdAt", p.getCreatedAt().toString());
                return m;
            }).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Grants PAID access: creates the enrollment if missing, else upgrades it.
    private void grantAccess(Payment payment) {
        User user = payment.getUser();
        Course course = payment.getCourse();

        Enrollment enrollment = enrollmentRepository
            .findByUserIdAndCourseId(user.getId(), course.getId())
            .orElse(null);

        if (enrollment == null) {
            enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .paymentStatus(Enrollment.PaymentStatus.PAID)
                .paymentReference(payment.getProviderRef())
                .build();
            course.setStudentCount(course.getStudentCount() + 1);
            courseRepository.save(course);
        } else {
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.PAID);
            enrollment.setPaymentReference(payment.getProviderRef());
        }
        enrollmentRepository.save(enrollment);
    }

    // Which provider handles a given method. Everything is MOCK until real keys
    // are added — then map e.g. MOBILE_MONEY -> PAYDUNYA/CINETPAY, PAYPAL -> PAYPAL.
    private String pickProvider(String method) {
        return "MOCK";
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}