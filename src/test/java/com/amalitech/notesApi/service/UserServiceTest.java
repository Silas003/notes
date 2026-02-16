package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.AuthRequest;
import com.amalitech.notesApi.dto.response.UserResponse;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.repository.UserRepository;
import com.amalitech.notesApi.security.JwtUtil;
import com.amalitech.notesApi.security.PasswordUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

    @Test
    void updateUser_success() {
        // Arrange
        Long userId = 1L;
        AuthRequest updateRequest = new AuthRequest("newemail@example.com", "newPassword");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("oldemail@example.com");
        existingUser.setPassword("oldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.updateUser(userId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("newemail@example.com", response.email());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_userNotFound_throwsException() {
        // Arrange
        Long userId = 99L;
        AuthRequest updateRequest = new AuthRequest("newemail@example.com", "newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userId, updateRequest));

        assertEquals("User not found", ex.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_userExists_returnsUserDetails() {
        // Arrange
        String email = "test@example.com";
        String password = "hashedPassword";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_userDoesNotExist_throwsException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(email));

        assertEquals("User not found with email: " + email, ex.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }
}