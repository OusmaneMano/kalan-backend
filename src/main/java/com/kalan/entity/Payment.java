package com.kalan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * A payment transaction (the money side). Kept separate from Enrollment:
 * Enrollment = access to a course; Payment = one attempt to pay for it.
 * A course can have several Payment rows (retries) but one Enrollment.
 */
@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private Double amount;          // e.g. 5000
    private String currency;        // XOF, EUR, USD

    private String method;          // MOBILE_MONEY, CARD, PAYPAL
    private String provider;        // MOCK, PAYDUNYA, CINETPAY, PAYPAL, STRIPE

    private String providerRef;     // provider transaction id / our reference

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Status {
        INITIATED, PENDING, SUCCESS, FAILED, CANCELLED
    }
}