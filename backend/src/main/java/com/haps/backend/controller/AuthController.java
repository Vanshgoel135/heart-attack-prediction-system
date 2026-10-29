package com.haps.backend.controller;

import com.haps.backend.dto.*;
import com.haps.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication, JWT token lifecycle, and profile management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new clinician/patient user account and issue JWT tokens")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and issue JWT access & refresh tokens")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate expired access token using valid refresh token")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset notification link")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        String msg = authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @GetMapping("/profile")
    @Operation(summary = "Retrieve current authenticated user profile")
    public ResponseEntity<UserProfileDto> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "email", required = false) String emailParam) {
        String identifier = (authHeader != null && !authHeader.isBlank()) ? authHeader : emailParam;
        if (identifier == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileDto profile = authService.getProfile(identifier);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current authenticated user profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "email", required = false) String emailParam,
            @Valid @RequestBody UserProfileDto profileDto) {
        String identifier = (authHeader != null && !authHeader.isBlank()) ? authHeader : emailParam;
        if (identifier == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileDto updated = authService.updateProfile(identifier, profileDto);
        return ResponseEntity.ok(updated);
    }
}
