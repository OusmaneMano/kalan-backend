package com.kalan.dto.request;

/** Body for POST /api/v1/payments/admin/grant */
public record AdminGrantRequest(
    String email,
    Long courseId
) {}