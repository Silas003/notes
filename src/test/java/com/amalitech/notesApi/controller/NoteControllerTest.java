package com.amalitech.notesApi.controller;


import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;



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
}

