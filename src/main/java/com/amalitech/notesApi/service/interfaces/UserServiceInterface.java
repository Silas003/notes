package com.amalitech.notesApi.service.interfaces;

import com.amalitech.notesApi.dto.request.AuthRequest;
import com.amalitech.notesApi.dto.response.AuthResponse;
import com.amalitech.notesApi.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserServiceInterface {
    void createUser(AuthRequest userRequest);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, AuthRequest userRequest);

    void deleteUser(Long id);

    AuthResponse loginUser(AuthRequest userRequest);

}
