package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.exception.RecordCountException;
import com.pc.kilojoulesrest.exception.RecordNameExistsException;
import com.pc.kilojoulesrest.exception.RecordNotDeletableException;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.ErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(RecordNotFoundException.class)
    protected ResponseEntity<Object> handleRecordNotFoundException(RecordNotFoundException ex, WebRequest request) {
        log.error("Record not found", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO(ex.getMessage()),
                new HttpHeaders(), HttpStatus.NOT_FOUND,
                request);
    }

    @ExceptionHandler(RecordNotDeletableException.class)
    protected ResponseEntity<Object> handleRecordNotDeletableException(RecordNotDeletableException ex, WebRequest request) {
        log.error("Record not deletable", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO(ex.getMessage()),
                new HttpHeaders(), HttpStatus.CONFLICT,
                request);
    }

    @ExceptionHandler(RecordCountException.class)
    protected ResponseEntity<Object> handleRecordCountException(RecordCountException ex, WebRequest request) {
        log.error("Record count exceeded", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(RecordNameExistsException.class)
    protected ResponseEntity<Object> handleRecordNameExistsException(RecordNameExistsException ex, WebRequest request) {
        log.error("Name already exists", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<Object> handleDataAccessException(
            DataAccessException ex,
            WebRequest request) {
        log.error("Database access error: ", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO(ex.getMessage()),
                new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE,
                request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {
        log.error("Authentication failed: ", ex);
        return handleExceptionInternal(ex,
                new ErrorDTO("Authentication failed." +
                        " Incorrect username and/or password."),
                new HttpHeaders(), HttpStatus.UNAUTHORIZED,
                request);
    }
}
