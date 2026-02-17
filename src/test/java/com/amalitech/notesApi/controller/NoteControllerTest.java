package com.amalitech.notesApi.controller;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.exceptions.GlobalExceptionHandler;
import com.amalitech.notesApi.exceptions.NoteNotFoundException;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    private ObjectMapper objectMapper;
    private Note note1;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(noteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        note1 = new Note(1L, "First Note", "Content of first note");
    }

    @Test
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/v1/notes/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notes API is running"));
    }

    @Test
    void shouldCreateNote() throws Exception {
        Note note = new Note();
        User user = new User();
        note.setId(1L);
        note.setTitle("Test");
        note.setContent("Test content");
        note.setUser(user);

        Mockito.when(noteService.createNote(any()))
                .thenReturn(note);

        String requestJson = """
        {
            "title": "Test",
            "content": "Test content"
        }
        """;

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    void shouldGetAllNotes() throws Exception {
        Note note = new Note();
        User user = new User();
        note.setId(1L);
        note.setTitle("Test");
        note.setContent("Content");
        note.setUser(user);

        Mockito.when(noteService.getAllNotes())
                .thenReturn(List.of(note));

        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetNoteById() throws Exception {
        Mockito.when(noteService.getNoteById(1L)).thenReturn(note1);

        mockMvc.perform(get("/api/v1/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First Note"))
                .andExpect(jsonPath("$.content").value("Content of first note"));
    }

    @Test
    void shouldReturnNotFoundWhenNoteDoesNotExist() throws Exception {
        Mockito.when(noteService.getNoteById(99L))
                .thenThrow(new NoteNotFoundException("Note with id 99 not found"));

        mockMvc.perform(get("/api/v1/notes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Note with id 99 not found"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentNote() throws Exception {
        NoteRequest request = new NoteRequest("Title", "Content");

        Mockito.when(noteService.updateNote(eq(999L), any(NoteRequest.class)))
                .thenThrow(new NoteNotFoundException("Note with id 999 not found"));

        mockMvc.perform(put("/api/v1/notes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Note with id 999 not found"));
    }

    @Test
    void shouldReturnBadRequestWhenTitleEmpty() throws Exception {
        NoteRequest request = new NoteRequest("", "Content");

        mockMvc.perform(put("/api/v1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateNoteSuccessfully() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Updated Title");
        note.setContent("Updated Content");

        NoteRequest request = new NoteRequest("Updated Title", "Updated Content");

        Mockito.when(noteService.updateNote(eq(1L), any(NoteRequest.class))).thenReturn(note);

        mockMvc.perform(put("/api/v1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    void shouldDeleteNoteSuccessfully() throws Exception {
        Mockito.doNothing().when(noteService).deleteNote(1L);

        mockMvc.perform(delete("/api/v1/notes/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Note with id 1 deleted successfully"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentNote() throws Exception {
        Mockito.doThrow(new NoteNotFoundException("Note with id 999 not found"))
                .when(noteService).deleteNote(999L);

        mockMvc.perform(delete("/api/v1/notes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Note with id 999 not found"));
    }
}