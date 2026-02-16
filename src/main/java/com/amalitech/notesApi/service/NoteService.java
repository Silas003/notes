package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.exceptions.InvalidNoteException;
import com.amalitech.notesApi.exceptions.NoteCreationException;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.repository.NoteRepository;
import com.amalitech.notesApi.service.interfaces.NoteServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NoteService implements NoteServiceInterface {
    private NoteRepository noteRepository;

    @Override
    public Note createNote(NoteRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidNoteException("Title cannot be empty");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new InvalidNoteException("Content cannot be empty");
        }

        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());

        try {
            return noteRepository.save(note);
        } catch (Exception ex) {
            throw new NoteCreationException("Failed to create note: " + ex.getMessage());
        }
    }

    @Override
    public List<Note> getAllNotes() {
        return noteRepository.findAll();    }

    @Override
    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }
}
