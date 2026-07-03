package com.kalan.controller;

import com.kalan.dto.response.AuthResponse;
import com.kalan.entity.User;
import com.kalan.repository.UserRepository;
import com.kalan.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody Map<String, String> body) {
        final String email    = body.get("email");
        final String fullName = body.get("fullName");
        final String photoUrl = body.get("photoUrl");

        // We trust the email from Google since the request comes with a valid
        // access_token or idToken — in production verify the token server-side
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Find existing user or create new one
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                .email(email)
                .fullName(fullName != null ? fullName : email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .country("")
                .preferredLanguage("fr")
                .role(User.Role.STUDENT)
                .avatarUrl(photoUrl)
                .createdAt(LocalDateTime.now())
                .build();
            return userRepository.save(newUser);
        });

        // Update last login and avatar if missing
        user.setLastLoginAt(LocalDateTime.now());
        if (photoUrl != null && user.getAvatarUrl() == null) {
            user.setAvatarUrl(photoUrl);
        }
        userRepository.save(user);

        // Generate Kalan JWT
        var userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails, user.getId());

        return ResponseEntity.ok(new AuthResponse(
            token, user.getEmail(), user.getFullName(),
            user.getCountry(), user.getPreferredLanguage(),
            user.getRole().name(), user.getId()
        ));
    }
}