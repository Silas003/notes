package com.amalitech.notesApi.controller;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.dto.response.NoteResponse;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.service.NoteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/notes")
public class NoteController {
    private final NoteService noteService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notes API is running");
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @Valid @RequestBody NoteRequest request) {

        Note note = noteService.createNote(request);

        NoteResponse response = new NoteResponse(note.getId(), note.getTitle(), note.getContent(), note.getCreatedAt(), note.getUpdatedAt());


        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Note> getNotes() {
        return noteService.getAllNotes();
    }

    @GetMapping("/{id}")
    public Note getNote(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }
}
