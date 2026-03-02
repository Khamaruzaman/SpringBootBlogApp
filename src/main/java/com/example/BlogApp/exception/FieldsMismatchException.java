package com.example.BlogApp.exception;

import lombok.Getter;

@Getter
public class FieldsMismatchException extends RuntimeException {
    private final String field;
    private final String fieldMatch;

    public FieldsMismatchException(String message, String field, String fieldMatch) {
        super(message);
        this.field = field;
        this.fieldMatch = fieldMatch;
    }

}

