package com.amalitech.notesApi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NoteRequest(@NotBlank String title, @NotBlank String content) {
}
