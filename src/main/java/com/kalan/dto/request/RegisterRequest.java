package com.kalan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ── Register request ──────────────────────────────────────────────────────────
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String fullName,
    String country,
    String preferredLanguage  // fr, en, bm, wo, pt
) {}
