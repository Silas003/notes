package com.amalitech.notesApi.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setup() {
        exceptionHandler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/notes/1");
    }

    @Test
    void shouldHandleNoteNotFoundException() {
        // Given
        NoteNotFoundException exception = new NoteNotFoundException("Note with id 1 not found");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNoteNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("details")).isEqualTo("Note with id 1 not found");
        assertThat(response.getBody().get("path")).isEqualTo("/api/v1/notes/1");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void shouldHandleInvalidNoteException() {
        // Given
        InvalidNoteException exception = new InvalidNoteException("Title cannot be empty");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidNote(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("details")).isEqualTo("Title cannot be empty");
        assertThat(response.getBody().get("path")).isEqualTo("/api/v1/notes/1");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        FieldError fieldError1 = new FieldError("noteRequest", "title", "Title is required");
        FieldError fieldError2 = new FieldError("noteRequest", "content", "Content is required");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Validation Failed");

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) response.getBody().get("details");
        assertThat(details).containsEntry("title", "Title is required");
        assertThat(details).containsEntry("content", "Content is required");
    }

    @Test
    void shouldHandleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", violations);

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConstraintViolation(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Validation Failed");
        assertThat(response.getBody().get("details")).isEqualTo("Constraint violation");
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid ID format");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Invalid ID format");
        assertThat(response.getBody().get("details")).isNull();
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWithNullMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException();

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Invalid parameter");
        assertThat(response.getBody().get("details")).isNull();
    }

    @Test
    void shouldHandleNoResourceFoundException() {
        // Given
        NoResourceFoundException exception = new NoResourceFoundException(
                HttpMethod.GET,
                "/api/v1/notes/123",
                "Resource not found"
        );
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNoResourceFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Resource Not Found");
        assertThat(response.getBody().get("details")).isNotNull();
    }

    @Test
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // When
        ResponseEntity<?> response = exceptionHandler.handleAccessDenied(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(403);
        assertThat(body.get("error")).isEqualTo("Forbidden");
        assertThat(body.get("details")).isEqualTo("Access is denied");
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        Exception exception = new Exception("Something went wrong");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("details")).isEqualTo("Something went wrong");
        assertThat(response.getBody().get("path")).isEqualTo("/api/v1/notes/1");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void shouldBuildResponseWithoutDetails() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Test error");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(exception, webRequest);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("details")).isNull();
        assertThat(response.getBody()).containsKeys("timestamp", "status", "error", "path");
    }

    @Test
    void shouldFormatPathCorrectly() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/notes/123");
        NoteNotFoundException exception = new NoteNotFoundException("Note not found");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNoteNotFound(exception, webRequest);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("path")).isEqualTo("/api/v1/notes/123");
    }
}