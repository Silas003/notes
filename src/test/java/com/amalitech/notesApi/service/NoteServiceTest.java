package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.exceptions.InvalidNoteException;
import com.amalitech.notesApi.exceptions.NoteNotFoundException;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.repository.NoteRepository;
import com.amalitech.notesApi.security.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private NoteService noteService;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");

        lenient().when(authenticatedUserService.getCurrentUser()).thenReturn(mockUser);
    }


    @Test
    void shouldCreateNoteSuccessfully() {
        NoteRequest request = new NoteRequest("Test Note", "Some content");
        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle(request.title());
        savedNote.setContent(request.content());
        savedNote.setUser(mockUser);

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        Note result = noteService.createNote(request);

        assertNotNull(result);
        assertEquals("Test Note", result.getTitle());
        assertEquals(mockUser, result.getUser());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void shouldThrowInvalidNoteExceptionWhenTitleEmpty() {
        NoteRequest request = new NoteRequest("", "Content");

        InvalidNoteException ex = assertThrows(InvalidNoteException.class,
                () -> noteService.createNote(request));

        assertEquals("Title cannot be empty", ex.getMessage());
    }

    @Test
    void shouldGetNoteByIdSuccessfully() {
        Note note = new Note(1L, "First Note", "Content");
        note.setUser(mockUser);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        Note result = noteService.getNoteById(1L);

        assertNotNull(result);
        assertEquals("First Note", result.getTitle());
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenIdDoesNotExist() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        NoteNotFoundException ex = assertThrows(NoteNotFoundException.class,
                () -> noteService.getNoteById(99L));

        assertEquals("Note not found", ex.getMessage());
    }

    @Test
    void shouldUpdateNoteSuccessfully() {
        Note existingNote = new Note(1L, "Old Title", "Old Content");
        existingNote.setUser(mockUser);

        NoteRequest request = new NoteRequest("Updated Title", "Updated Content");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, request);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        verify(noteRepository, times(1)).save(existingNote);
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenUpdatingNonExistentNote() {
        NoteRequest request = new NoteRequest("Title", "Content");
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        NoteNotFoundException ex = assertThrows(NoteNotFoundException.class,
                () -> noteService.updateNote(999L, request));

        assertEquals("Note not found", ex.getMessage());
    }

    @Test
    void shouldDeleteNoteSuccessfully() {
        Note note = new Note(1L, "Delete Me", "Content");
        note.setUser(mockUser);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        assertDoesNotThrow(() -> noteService.deleteNote(1L));
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenDeletingNonExistentNote() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        NoteNotFoundException ex = assertThrows(NoteNotFoundException.class,
                () -> noteService.deleteNote(999L));

        assertEquals("Note not found", ex.getMessage());
    }

}
