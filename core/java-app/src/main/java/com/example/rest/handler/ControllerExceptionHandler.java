package com.example.rest.handler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.rest.exception.AlreadyExistException;
import com.example.rest.exception.NotFoundException;
import com.example.rest.exception.UnauthorizedException;
import com.example.rest.response.ErrorResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handle(NotFoundException exception) {

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            List.of(exception.getMessage()),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<?> handle(AlreadyExistException exception) {

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            List.of(exception.getMessage()),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handle(UnauthorizedException exception) {

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            List.of(exception.getMessage()),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handle(MethodArgumentNotValidException exception) {

        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getDefaultMessage()).collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            errors,
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handle(ConstraintViolationException exception) {

        List<String> errors = exception.getConstraintViolations().stream()
            .map(error -> error.getMessage()).collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            errors,
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
