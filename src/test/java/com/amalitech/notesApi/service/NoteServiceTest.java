package com.amalitech.notesApi.service;

import com.amalitech.notesApi.dto.request.NoteRequest;
import com.amalitech.notesApi.exceptions.InvalidNoteException;
import com.amalitech.notesApi.exceptions.NoteCreationException;
import com.amalitech.notesApi.models.Note;
import com.amalitech.notesApi.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void shouldHaveRepositoryInitialized() {
        assertThat(noteRepository).isNotNull();
        assertThat(noteService).isNotNull();
    }


    @Test
    void shouldCreateNoteSuccessfully() {
        NoteRequest request = new NoteRequest("Test Note", "This is a valid note");


        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Test Note");
        savedNote.setContent("This is a valid note");

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        Note result = noteService.createNote(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Note");

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getTitle()).isEqualTo("Test Note");
    }

    @Test
    void shouldThrowInvalidNoteExceptionForEmptyTitle() {
        NoteRequest request = new NoteRequest("", "Content");

        assertThrows(InvalidNoteException.class, () -> noteService.createNote(request));
    }

    @Test
    void shouldThrowNoteCreationExceptionWhenRepositoryFails() {
        NoteRequest request = new NoteRequest("Test Note", "This is a valid note");


        when(noteRepository.save(any(Note.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(NoteCreationException.class, () -> noteService.createNote(request));
    }

    @Test
    void shouldThrowInvalidNoteExceptionForNullContent() {
        NoteRequest request = new NoteRequest("Valid Title", null); // invalid content


        assertThrows(InvalidNoteException.class, () -> noteService.createNote(request));
    }


    @Test
    void shouldReturnAllNotesSuccessfully() {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Note 1");
        note.setContent("Content 1");

        when(noteRepository.findAll()).thenReturn(List.of(note));

        List<Note> notes = noteService.getAllNotes();
        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getTitle()).isEqualTo("Note 1");
    }
}
