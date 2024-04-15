package com.pc.kilojoulesrest.exception;

public class RecordNameExistsException extends RuntimeException {
    public RecordNameExistsException(String message) {
        super(message);
    }
}
