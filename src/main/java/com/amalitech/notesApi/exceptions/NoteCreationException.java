package com.amalitech.notesApi.exceptions;

public class NoteCreationException extends RuntimeException {
    public NoteCreationException(String message) {
        super(message);
    }
}
