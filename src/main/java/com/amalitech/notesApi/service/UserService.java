package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.AuthRequest;
import com.amalitech.notesApi.dto.response.AuthResponse;
import com.amalitech.notesApi.dto.response.UserResponse;
import com.amalitech.notesApi.exceptions.UserExists;
import com.amalitech.notesApi.mapper.UserMapper;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.repository.UserRepository;
import com.amalitech.notesApi.security.JwtUtil;
import com.amalitech.notesApi.security.PasswordUtils;
import com.amalitech.notesApi.service.interfaces.UserServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserService implements UserServiceInterface , UserDetailsService {
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private UserMapper userMapper;
    @Override
    public void createUser(AuthRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new UserExists("User with given email or username already exists");
        }
        String password = PasswordUtils.hashPassword(userRequest.password());
        User user = new User(userRequest.email(),password);
        userRepository.save(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
       User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );
        return  new UserResponse(user.getEmail());
    }
    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return  users.stream()
                .map(user -> new UserResponse(user.getEmail()))
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, AuthRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String password = PasswordUtils.hashPassword(userRequest.password());


        existingUser.setEmail(userRequest.email());
        existingUser.setPassword(password);
        userRepository.save(existingUser);
        return new UserResponse(existingUser.getEmail());
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        userRepository.deleteById(existingUser.getId());
    }

    @Override
    public AuthResponse loginUser(AuthRequest userRequest) {
        String email = userRequest.email();
        String password = userRequest.password();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            boolean authenticated = PasswordUtils.verifyPassword(password, user.getPassword());
            if (!authenticated) {
                throw new IllegalArgumentException("Invalid credentials");
            } else {
                String token = jwtUtil.generateToken(user);
                return new AuthResponse(token);
            }
        } else {
            throw new IllegalArgumentException("User with given email does not exist");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + username)
        );
        Collection<? extends GrantedAuthority> authorities = List.of(

        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

    }
}


