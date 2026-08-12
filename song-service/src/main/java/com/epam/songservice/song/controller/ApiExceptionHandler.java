package com.epam.songservice.song.controller;

import com.epam.songservice.song.exception.SongMetadataConflictException;
import com.epam.songservice.song.exception.SongMetadataNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
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

  private static final String VALIDATION_ERROR = "Validation error";
  private static final String INVALID_REQUEST = "Invalid request";
  private static final String INTERNAL_ERROR = "An error occurred on the server";
  private static final String ERROR_CODE_400 = "400";
  private static final String ERROR_CODE_404 = "404";
  private static final String ERROR_CODE_409 = "409";
  private static final String ERROR_CODE_500 = "500";
  private static final int MAX_ID_CSV_LENGTH = 200;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    log.warn("Validation failed: {}", ex.getMessage());
    return validationErrorResponse(collectValidationDetails(ex));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("Type mismatch error: {}", ex.getMessage());
    String value = ex.getValue() == null ? "null" : ex.getValue().toString();
    String errorMessage = "Invalid value '" + value + "' for ID. Must be a positive integer";
    return errorResponse(HttpStatus.BAD_REQUEST, errorMessage, ERROR_CODE_400);
  }

  @ExceptionHandler({
      ConstraintViolationException.class,
      MissingServletRequestParameterException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
    log.warn("Bad request: {}", ex.getMessage());
    if (ex instanceof ConstraintViolationException constraintViolationException) {
      String errorMessage = extractConstraintViolationMessage(constraintViolationException);
      return errorResponse(HttpStatus.BAD_REQUEST, errorMessage, ERROR_CODE_400);
    }
    return badRequestResponse();
  }

  @ExceptionHandler(SongMetadataNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(SongMetadataNotFoundException ex) {
    log.warn("Song metadata not found: {}", ex.getMessage());
    return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ERROR_CODE_404);
  }

  @ExceptionHandler(SongMetadataConflictException.class)
  public ResponseEntity<ApiErrorResponse> handleConflict(SongMetadataConflictException ex) {
    log.warn("Song metadata conflict: {}", ex.getMessage());
    return errorResponse(HttpStatus.CONFLICT, ex.getMessage(), ERROR_CODE_409);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleInternal(Exception ex) {
    log.error("Unhandled exception", ex);
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR, ERROR_CODE_500);
  }

  private ResponseEntity<ApiErrorResponse> badRequestResponse() {
    return errorResponse(HttpStatus.BAD_REQUEST, INVALID_REQUEST, ERROR_CODE_400, null);
  }

  private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, String errorCode) {
    return errorResponse(status, message, errorCode, null);
  }

  private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status,
      String message,
      String errorCode,
      Map<String, String> details) {
    return ResponseEntity.status(status).body(new ApiErrorResponse(message, errorCode, details));
  }

  private ResponseEntity<ApiErrorResponse> validationErrorResponse(Map<String, String> details) {
    return errorResponse(HttpStatus.BAD_REQUEST, VALIDATION_ERROR, ERROR_CODE_400, details);
  }

  private String extractConstraintViolationMessage(ConstraintViolationException ex) {
    return ex.getConstraintViolations().stream()
        .findFirst()
        .map(violation -> {
          Object invalidValue = violation.getInvalidValue();
          if (invalidValue instanceof String value) {
            if (value.length() > MAX_ID_CSV_LENGTH) {
              return "CSV string is too long: received " + value.length()
                  + " characters, maximum allowed is " + MAX_ID_CSV_LENGTH;
            }
            if (!value.matches("^\\d+(,\\d+)*$")) {
              return "Invalid ID format: '" + findInvalidCsvToken(value)
                  + "'. Only positive integers are allowed";
            }
          }
          String value = invalidValue == null ? "null" : invalidValue.toString();
          return "Invalid value '" + value + "' for ID. Must be a positive integer";
        })
        .orElse(INVALID_REQUEST);
  }

  private String findInvalidCsvToken(String csv) {
    String[] parts = csv.split(",");
    for (String part : parts) {
      if (!part.matches("\\d+")) {
        return part;
      }
    }
    return csv;
  }

  private Map<String, String> collectValidationDetails(MethodArgumentNotValidException exception) {
    Map<String, String> details = new LinkedHashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return details;
  }
}

