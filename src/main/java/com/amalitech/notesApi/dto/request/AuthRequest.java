package com.amalitech.notesApi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AuthRequest(@NotNull @Email String  email,@NotNull @Min(value = 8,message = "password must not be less than 8 characters") String password) {
}
