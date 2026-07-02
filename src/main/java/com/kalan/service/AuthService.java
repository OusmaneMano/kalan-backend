package com.kalan.service;

import com.kalan.dto.request.LoginRequest;
import com.kalan.dto.request.RegisterRequest;
import com.kalan.dto.response.AuthResponse;
import com.kalan.entity.User;
import com.kalan.repository.UserRepository;
import com.kalan.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .fullName(req.fullName())
            .country(req.country() != null ? req.country() : "")
            .preferredLanguage(req.preferredLanguage() != null ? req.preferredLanguage() : "fr")
            .role(User.Role.STUDENT)
            .createdAt(LocalDateTime.now())
            .build();

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId());

        return buildResponse(token, user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId());

        return buildResponse(token, user);
    }

    private AuthResponse buildResponse(String token, User user) {
        return new AuthResponse(
            token,
            user.getEmail(),
            user.getFullName(),
            user.getCountry(),
            user.getPreferredLanguage(),
            user.getRole().name(),
            user.getId()
        );
    }
}