package com.epam.songservice.song.controller;

import com.epam.songservice.song.exception.SongMetadataConflictException;
import com.epam.songservice.song.exception.SongMetadataNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  private static final String REQUEST_VALIDATION_FAILED = "Request validation failed";
  private static final String INVALID_REQUEST = "Invalid request";
  private static final String INTERNAL_ERROR = "An error occurred on the server";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    log.warn("Validation failed: {}", ex.getMessage());
    return badRequestResponse(firstValidationErrorMessage(ex));
  }

  @ExceptionHandler({
      ConstraintViolationException.class,
      MethodArgumentTypeMismatchException.class,
      MissingServletRequestParameterException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return badRequestResponse(INVALID_REQUEST);
  }

  @ExceptionHandler(SongMetadataNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(SongMetadataNotFoundException ex) {
    log.warn("Song metadata not found: {}", ex.getMessage());
    return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(SongMetadataConflictException.class)
  public ResponseEntity<ApiErrorResponse> handleConflict(SongMetadataConflictException ex) {
    log.warn("Song metadata conflict: {}", ex.getMessage());
    return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleInternal(Exception ex) {
    log.error("Unhandled exception", ex);
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR);
  }

  private ResponseEntity<ApiErrorResponse> badRequestResponse(String message) {
    return errorResponse(HttpStatus.BAD_REQUEST, message);
  }

  private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ApiErrorResponse(message));
  }

  private String firstValidationErrorMessage(MethodArgumentNotValidException exception) {
    return exception.getBindingResult().getFieldErrors().stream()
        .map(this::mapFieldError)
        .findFirst()
        .orElse(REQUEST_VALIDATION_FAILED);
  }

  private String mapFieldError(FieldError fieldError) {
    return fieldError.getField() + " " + fieldError.getDefaultMessage();
  }
}

