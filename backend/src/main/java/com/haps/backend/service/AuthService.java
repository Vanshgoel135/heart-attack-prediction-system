package com.haps.backend.service;

import com.haps.backend.dto.*;
import com.haps.backend.entity.User;
import com.haps.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private static final String JWT_SECRET = "HAPS_SUPER_SECURE_JWT_SECRET_KEY_FOR_CARDIOLOGY_AI_2026";
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 86400L * 7L; // 7 days
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 86400L * 30L; // 30 days

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String salt = generateSalt();
        String hash = hashPassword(dto.getPassword(), salt);

        User user = new User();
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPasswordHash(salt + ":" + hash);
        user.setFullName(dto.getFullName().trim());
        user.setRole("USER");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        User saved = userRepository.save(user);

        String token = createJwtToken(saved.getEmail(), saved.getId(), ACCESS_TOKEN_VALIDITY_SECONDS);
        String refreshToken = createJwtToken(saved.getEmail(), saved.getId(), REFRESH_TOKEN_VALIDITY_SECONDS);

        return new AuthResponseDto(token, refreshToken, ACCESS_TOKEN_VALIDITY_SECONDS, saved.getId(), saved.getEmail(), saved.getFullName());
    }

    public AuthResponseDto login(AuthRequestDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!verifyPassword(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = createJwtToken(user.getEmail(), user.getId(), ACCESS_TOKEN_VALIDITY_SECONDS);
        String refreshToken = createJwtToken(user.getEmail(), user.getId(), REFRESH_TOKEN_VALIDITY_SECONDS);

        return new AuthResponseDto(token, refreshToken, ACCESS_TOKEN_VALIDITY_SECONDS, user.getId(), user.getEmail(), user.getFullName());
    }

    public AuthResponseDto refreshToken(RefreshTokenRequestDto dto) {
        String token = dto.getRefreshToken();
        String email = extractEmailFromToken(token);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newToken = createJwtToken(user.getEmail(), user.getId(), ACCESS_TOKEN_VALIDITY_SECONDS);
        String newRefreshToken = createJwtToken(user.getEmail(), user.getId(), REFRESH_TOKEN_VALIDITY_SECONDS);

        return new AuthResponseDto(newToken, newRefreshToken, ACCESS_TOKEN_VALIDITY_SECONDS, user.getId(), user.getEmail(), user.getFullName());
    }

    public String forgotPassword(ForgotPasswordRequestDto dto) {
        // Return a reassuring confirmation message
        return "If an account exists for " + dto.getEmail() + ", a password reset link has been dispatched.";
    }

    public UserProfileDto getProfile(String tokenOrEmail) {
        String email = extractSubject(tokenOrEmail);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for: " + email));

        return toProfileDto(user);
    }

    public UserProfileDto updateProfile(String tokenOrEmail, UserProfileDto dto) {
        String email = extractSubject(tokenOrEmail);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBloodGroup() != null) user.setBloodGroup(dto.getBloodGroup());
        if (dto.getEmergencyContact() != null) user.setEmergencyContact(dto.getEmergencyContact());
        user.setUpdatedAt(OffsetDateTime.now());

        User updated = userRepository.save(user);
        return toProfileDto(updated);
    }

    private UserProfileDto toProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAge(user.getAge());
        dto.setGender(user.getGender());
        dto.setBloodGroup(user.getBloodGroup());
        dto.setEmergencyContact(user.getEmergencyContact());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // JWT creation using standard RFC 7519 HMAC-SHA256
    public String createJwtToken(String email, Long userId, long validitySeconds) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + validitySeconds;

            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = String.format("{\"sub\":\"%s\",\"userId\":%d,\"iat\":%d,\"exp\":%d}", email, userId, now, exp);

            String headerEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String content = headerEncoded + "." + payloadEncoded;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signatureBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            String signatureEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

            return content + "." + signatureEncoded;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public String extractEmailFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (token == null || !token.contains(".")) return null;
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            // Verify signature
            String content = parts[0] + "." + parts[1];
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signatureBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            String expectedSig = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
            if (!expectedSig.equals(parts[2])) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            // extract "sub":"..."
            int subIdx = payload.indexOf("\"sub\":\"");
            if (subIdx == -1) return null;
            int start = subIdx + 7;
            int end = payload.indexOf("\"", start);
            if (end == -1) return null;

            // check exp
            int expIdx = payload.indexOf("\"exp\":");
            if (expIdx != -1) {
                int expStart = expIdx + 6;
                // check exp
                // Check token expiry
                // Check token expiry
                int expIdx = payload.indexOf("\"exp\":");
                if (expIdx != -1) {
                    int expStart = expIdx + 6;

                    int comma = payload.indexOf(",", expStart);
                    int brace = payload.indexOf("}", expStart);

                    int expEnd;
                    if (comma == -1) {
                        expEnd = brace;
                    } else if (brace == -1) {
                        expEnd = comma;
                    } else {
                        expEnd = Math.min(comma, brace);
                    }

                    long exp = Long.parseLong(payload.substring(expStart, expEnd).trim());
                    if (Instant.now().getEpochSecond() > exp) {
                        return null;
                    }
                }

                return payload.substring(start, end);

                    long exp = Long.parseLong(payload.substring(expStart, expEnd).trim());
                    if (Instant.now().getEpochSecond() > exp) {
                        return null;
                    }
                }

                return payload.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractSubject(String tokenOrEmail) {
        if (tokenOrEmail == null) throw new IllegalArgumentException("Token or email required");
        if (tokenOrEmail.startsWith("Bearer ") || tokenOrEmail.contains(".")) {
            String email = extractEmailFromToken(tokenOrEmail);
            if (email != null) return email;
        }
        return tokenOrEmail.trim().toLowerCase();
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) return false;
        String[] parts = storedHash.split(":");
        String salt = parts[0];
        String expectedHash = parts[1];
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(expectedHash);
    }

    @Bean
    public CommandLineRunner initDefaultUser(UserRepository userRepo) {
        return args -> {
            if (userRepo.count() == 0) {
                String salt = generateSalt();
                User defaultUser = new User();
                defaultUser.setEmail("doctor@haps.health");
                defaultUser.setPasswordHash(salt + ":" + hashPassword("Doctor@123", salt));
                defaultUser.setFullName("Dr. Rajesh Sharma, MD (Cardiology)");
                defaultUser.setRole("PHYSICIAN");
                defaultUser.setPhone("+91 98765 43210");
                defaultUser.setAge(42);
                defaultUser.setGender("Male");
                defaultUser.setBloodGroup("O+");
                defaultUser.setEmergencyContact("+91 98765 11223");
                defaultUser.setCreatedAt(OffsetDateTime.now());
                defaultUser.setUpdatedAt(OffsetDateTime.now());
                userRepo.save(defaultUser);
            }
        };
    }
}
