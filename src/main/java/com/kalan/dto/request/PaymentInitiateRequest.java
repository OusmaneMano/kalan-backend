package com.kalan.dto.request;

/**
 * Body for POST /api/v1/payments/initiate.
 * method: MOBILE_MONEY | CARD | PAYPAL
 */
public record PaymentInitiateRequest(
    Long courseId,
    String method
) {}