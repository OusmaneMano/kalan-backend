package com.kalan.dto.response;

public record AuthResponse(
    String token,
    String email,
    String fullName,
    String country,
    String preferredLanguage,
    String role,
    Long userId
) {}
