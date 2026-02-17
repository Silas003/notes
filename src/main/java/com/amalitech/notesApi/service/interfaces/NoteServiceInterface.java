package com.amalitech.notesApi.service.interfaces;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.models.Note;

import java.util.List;

public interface  NoteServiceInterface {
    Note createNote(NoteRequest note);
    List<Note> getAllNotes();
    Note getNoteById(Long id);
    Note updateNote(Long id, NoteRequest note);
    void deleteNote(Long id);
}
