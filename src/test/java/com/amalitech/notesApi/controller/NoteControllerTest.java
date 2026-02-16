package com.amalitech.notesApi.controller;


import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.exceptions.InvalidNoteException;
import com.amalitech.notesApi.exceptions.NoteNotFoundException;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@Import(com.fasterxml.jackson.databind.ObjectMapper.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteService noteService;

    private Note note1;
    private Note note2;



    @BeforeEach
    void setup() {
            note1 = new Note(1L, "First Note", "Content of first note");
          note2 = new Note(2L, "Second Note", "Content of second note");
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
        note.setId(1L);
        note.setTitle("Test");
        note.setContent("Test content");
        note.setUserId(1L);

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
        note.setId(1L);
        note.setTitle("Test");
        note.setContent("Content");
        note.setUserId(1L);

        Mockito.when(noteService.getAllNotes())
                .thenReturn(List.of(note));

        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetNoteById() throws Exception {
        Mockito.when(noteService.getNoteById(1L)).thenReturn(note1);

        mockMvc.perform(get("/api/v1/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First Note"))
                .andExpect(jsonPath("$.content").value("Content of first note"));
    }

    @Test
    void testGetNoteById_NotFound() throws Exception {
        Mockito.when(noteService.getNoteById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/notes/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldReturnNotFoundWhenNoteDoesNotExist() throws Exception {
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

        Mockito.when(noteService.updateNote(eq(1L), any(NoteRequest.class)))
                .thenThrow(new InvalidNoteException("Title cannot be empty"));

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
}

