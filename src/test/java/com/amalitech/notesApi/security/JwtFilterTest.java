package com.amalitech.notesApi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private UserDetails userDetails;
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final String USERNAME = "testuser@example.com";

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();

        userDetails = User.builder()
                .username(USERNAME)
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void shouldContinueFilterChainWhenNoAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractSubject(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractSubject(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldContinueFilterChainWhenTokenExtractionFails() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(jwtUtil.extractSubject(INVALID_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldAuthenticateUserWhenValidTokenProvided() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractSubject(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(VALID_TOKEN, USERNAME)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractSubject(VALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(jwtUtil).isTokenValid(VALID_TOKEN, USERNAME);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(jwtUtil.extractSubject(INVALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(INVALID_TOKEN, USERNAME)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractSubject(INVALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(jwtUtil).isTokenValid(INVALID_TOKEN, USERNAME);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateWhenSubjectIsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractSubject(VALID_TOKEN)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractSubject(VALID_TOKEN);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtUtil, never()).isTokenValid(any(), any());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateWhenAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                )
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractSubject(VALID_TOKEN)).thenReturn(USERNAME);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractSubject(VALID_TOKEN);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtUtil, never()).isTokenValid(any(), any());
    }

    @Test
    void shouldExtractTokenCorrectlyFromBearerHeader() throws ServletException, IOException {
        // Given
        String fullToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";

        when(request.getHeader("Authorization")).thenReturn(fullToken);
        when(jwtUtil.extractSubject(expectedToken)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(expectedToken, USERNAME)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).extractSubject(expectedToken);
        verify(jwtUtil).isTokenValid(expectedToken, USERNAME);
    }

    @Test
    void shouldHandleUserDetailsServiceException() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractSubject(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME))
                .thenThrow(new RuntimeException("User not found"));

        // When/Then
        try {
            jwtFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
        }

        verify(jwtUtil).extractSubject(VALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(jwtUtil, never()).isTokenValid(any(), any());
    }

    @Test
    void shouldSetAuthenticationDetailsFromRequest() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractSubject(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(VALID_TOKEN, USERNAME)).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getDetails()).isNotNull();
    }

    @Test
    void shouldHandleEmptyBearerToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        // The filter will try to extract subject from empty string, which should fail
    }

    @Test
    void shouldPreserveUserAuthorities() throws ServletException, IOException {
        // Given
        UserDetails userWithMultipleRoles = User.builder()
                .username(USERNAME)
                .password("password")
                .authorities(
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                )
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractSubject(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userWithMultipleRoles);
        when(jwtUtil.isTokenValid(VALID_TOKEN, USERNAME)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .hasSize(2)
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}