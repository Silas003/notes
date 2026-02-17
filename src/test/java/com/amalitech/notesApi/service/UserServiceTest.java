package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.AuthRequest;
import com.amalitech.notesApi.dto.response.UserResponse;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.repository.UserRepository;
import com.amalitech.notesApi.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserFound() {
        User mockUser = new User();
        mockUser.setId(1L);

        mockUser.setEmail("email@gmail.com");
        mockUser.setPassword("Testpassword");

        UserResponse expectedResponse = new UserResponse(
                "email@gmail.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);

        assertEquals("email@gmail.com", result.email());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);

    }

    @Test
    void shouldReturnUsers() {
        User user = new User();
        user.setId(1L);
        user.setPassword("Testpassword");
        user.setEmail("email@gmail.com");
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        UserResponse expectedResponse = new UserResponse(
                "email@gmail.com"
        );

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("email@gmail.com", result.get(0).email());

        verify(userRepository, times(1)).findAll();

    }

    @Test
    void shouldDeleteUserWhenExists() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldCreateUser() {
        AuthRequest userRequest = new AuthRequest(
                "email@gmail.com",
                "Testpassword"
        );

        when(userRepository.existsByEmail(userRequest.email()))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(userRequest);

        verify(userRepository, times(1))
                .existsByEmail(userRequest.email());

        verify(userRepository, times(1))
                .save(any(User.class));
    }


    @Test
    void shouldThrowExceptionWithInvalidCredentialsAfterLogin() {
        User user = new User();
        user.setId(1L);
        user.setPassword("Testpassword");
        user.setEmail("email@gmail.com");

        AuthRequest userRequest = new AuthRequest(user.getEmail(), "wrong-password");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.loginUser(userRequest));
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void getUserById_usesRepositoryOnce_whenCalledTwice() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserResponse response = new UserResponse( "test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse first = userService.getUserById(1L);
        UserResponse second = userService.getUserById(1L);

        assertNotNull(first);
        assertNotNull(second);

        verify(userRepository, times(2)).findById(1L);
    }
}