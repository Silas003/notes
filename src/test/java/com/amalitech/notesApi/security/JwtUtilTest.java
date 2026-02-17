package com.amalitech.notesApi.security;

import com.amalitech.notesApi.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    private static final String SECRET_KEY = "change-me-secret-key-change-me-secret-key-change-me-secret-key";
    private static final long EXPIRATION_MS = 3600000; // 1 hour
    private static final String ISSUER = "test-app";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil(SECRET_KEY, EXPIRATION_MS, ISSUER);

        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword("password123");
    }

    @Test
    void shouldGenerateValidToken() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void shouldExtractSubjectFromToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String subject = jwtUtil.extractSubject(token);

        // Then
        assertThat(subject).isEqualTo(TEST_EMAIL);
    }

    @Test
    void shouldValidateTokenWithCorrectSubject() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, TEST_EMAIL);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateTokenWithIncorrectSubject() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, "wrong@example.com");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidateExpiredToken() {
        // Given - Create JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil(SECRET_KEY, 1, ISSUER); // 1ms expiration
        String token = shortExpirationJwtUtil.generateToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When/Then
        assertThatThrownBy(() -> shortExpirationJwtUtil.isTokenValid(token, TEST_EMAIL))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractSubject(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void shouldThrowExceptionForTokenWithInvalidSignature() {
        // Given
        String token = jwtUtil.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractSubject(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldIncludeIssuerInToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
    }

    @Test
    void shouldIncludeIssuedAtDateInToken() {
        // Given
        long beforeGeneration = System.currentTimeMillis() / 1000 * 1000; // Round down to seconds
        String token = jwtUtil.generateToken(testUser);
        long afterGeneration = System.currentTimeMillis() / 1000 * 1000 + 1000; // Round up to next second

        // When
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getIssuedAt().getTime())
                .isGreaterThanOrEqualTo(beforeGeneration)
                .isLessThanOrEqualTo(afterGeneration);
    }

    @Test
    void shouldIncludeExpirationDateInToken() {
        // Given
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(testUser);

        // When
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        Date expectedExpiration = new Date(beforeGeneration.getTime() + EXPIRATION_MS);
        assertThat(claims.getExpiration()).isNotNull();
        // Allow 1 second difference due to test execution time
        assertThat(claims.getExpiration().getTime())
                .isCloseTo(expectedExpiration.getTime(), org.assertj.core.data.Offset.offset(1000L));
    }

    @Test
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // Given/When
        String token1 = jwtUtil.generateToken(testUser);
        Thread.sleep(1000); // Wait 1 second to ensure different timestamps (JWT uses seconds, not milliseconds)
        String token2 = jwtUtil.generateToken(testUser);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password456");

        // When
        String token1 = jwtUtil.generateToken(testUser);
        String token2 = jwtUtil.generateToken(anotherUser);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extractSubject(token1)).isEqualTo(TEST_EMAIL);
        assertThat(jwtUtil.extractSubject(token2)).isEqualTo("another@example.com");
    }

    @Test
    void shouldHandleEmptyStringToken() {
        // Given
        String emptyToken = "";

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractSubject(emptyToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleNullToken() {
        // Given
        String nullToken = null;

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractSubject(nullToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseCustomSecretKey() {
        // Given
        String customSecret = "my-custom-secret-key-my-custom-secret-key-my-custom-secret-key";
        JwtUtil customJwtUtil = new JwtUtil(customSecret, EXPIRATION_MS, ISSUER);
        String token = customJwtUtil.generateToken(testUser);

        // When
        String subject = customJwtUtil.extractSubject(token);

        // Then
        assertThat(subject).isEqualTo(TEST_EMAIL);

        // Token generated with custom secret should not be valid with default JwtUtil
        assertThatThrownBy(() -> jwtUtil.extractSubject(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldUseCustomExpiration() {
        // Given
        long customExpiration = 7200000; // 2 hours
        JwtUtil customJwtUtil = new JwtUtil(SECRET_KEY, customExpiration, ISSUER);
        String token = customJwtUtil.generateToken(testUser);

        // When
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        long actualDuration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(actualDuration).isCloseTo(customExpiration, org.assertj.core.data.Offset.offset(1000L));
    }

    @Test
    void shouldUseCustomIssuer() {
        // Given
        String customIssuer = "custom-issuer";
        JwtUtil customJwtUtil = new JwtUtil(SECRET_KEY, EXPIRATION_MS, customIssuer);
        String token = customJwtUtil.generateToken(testUser);

        // When
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        assertThat(claims.getIssuer()).isEqualTo(customIssuer);
    }

    @Test
    void shouldValidateTokenWithNullSubject() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldHandleTokenSignedWithDifferentAlgorithm() {
        // Given - Create a token with different secret
        SecretKey differentKey = Keys.hmacShaKeyFor(
                "different-secret-key-different-secret-key-different-secret".getBytes(StandardCharsets.UTF_8)
        );

        String tokenWithDifferentKey = Jwts.builder()
                .subject(TEST_EMAIL)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(differentKey)
                .compact();

        // When/Then - Should fail signature verification
        assertThatThrownBy(() -> jwtUtil.extractSubject(tokenWithDifferentKey))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldReturnFalseForTokenWithNullExpiration() {
        // Given - Create a token without expiration (manually constructed)
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String tokenWithoutExpiration = Jwts.builder()
                .subject(TEST_EMAIL)
                .issuer(ISSUER)
                .issuedAt(new Date())
                // No expiration set
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtUtil.isTokenValid(tokenWithoutExpiration, TEST_EMAIL);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldValidateTokenIsNotExpiredYet() throws InterruptedException {
        // Given
        JwtUtil shortLivedJwtUtil = new JwtUtil(SECRET_KEY, 2000, ISSUER); // 2 seconds
        String token = shortLivedJwtUtil.generateToken(testUser);

        // When - Check immediately
        boolean isValidNow = shortLivedJwtUtil.isTokenValid(token, TEST_EMAIL);

        // Wait but not long enough to expire
        Thread.sleep(500);
        boolean isValidAfterWait = shortLivedJwtUtil.isTokenValid(token, TEST_EMAIL);

        // Then
        assertThat(isValidNow).isTrue();
        assertThat(isValidAfterWait).isTrue();
    }
}