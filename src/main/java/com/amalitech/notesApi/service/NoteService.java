package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.exceptions.InvalidNoteException;
import com.amalitech.notesApi.exceptions.NoteCreationException;
import com.amalitech.notesApi.exceptions.NoteNotFoundException;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.repository.NoteRepository;
import com.amalitech.notesApi.security.AuthenticatedUserService;
import com.amalitech.notesApi.service.interfaces.NoteServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NoteService implements NoteServiceInterface {
    private NoteRepository noteRepository;
    private AuthenticatedUserService authenticatedUserService;
    @Override
    public Note createNote(NoteRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidNoteException("Title cannot be empty");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new InvalidNoteException("Content cannot be empty");
        }
        User user = authenticatedUserService.getCurrentUser();

        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setUser(user);

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
        User user = authenticatedUserService.getCurrentUser();

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this note");
        }

        return note;
    }

    @Override
    public Note updateNote(Long id, NoteRequest request) {


        if (request.title() == null || request.title().isBlank()) {
                throw new InvalidNoteException("Title cannot be empty");
            }
            if (request.content() == null || request.content().isBlank()) {
                throw new InvalidNoteException("Content cannot be empty");
            }

        Note existingNote = getNoteById(id);

        existingNote.setTitle(request.title());
            existingNote.setContent(request.content());

            return noteRepository.save(existingNote);
        }

    @Override
    public void deleteNote(Long id) {
        Note note = getNoteById(id);
        noteRepository.delete(note);
    }

}
