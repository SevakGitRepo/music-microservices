package com.epam.resourceservice.exception;

import com.epam.resourceservice.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final String VALIDATION_ERROR_MESSAGE = "Validation error";
  private static final String VALIDATION_ERROR_CODE = "400";
  private static final String NOT_FOUND_ERROR_CODE = "404";
  private static final String INTERNAL_ERROR_CODE = "500";
  private static final String INTERNAL_ERROR_MESSAGE = "An error occurred on the server";
  private static final String INVALID_MP3_MESSAGE = "The request body is invalid MP3";
  private static final String UNSUPPORTED_FORMAT_MESSAGE =
      "Invalid file format: %s. Only MP3 files are allowed";

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleStructuredValidation(ValidationException exception) {
    logValidationWarning(exception.getMessage());
    return validationErrorResponse(exception.getDetails());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException exception) {
    log.warn("Bad request: {}", exception.getMessage());
    return errorResponse(HttpStatus.BAD_REQUEST,
        new ErrorResponse(exception.getMessage(), VALIDATION_ERROR_CODE, null));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(
      HttpMessageNotReadableException exception) {
    log.warn("Unreadable request body: {}", exception.getMessage());
    return errorResponse(HttpStatus.BAD_REQUEST,
        new ErrorResponse(INVALID_MP3_MESSAGE, VALIDATION_ERROR_CODE, null));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException exception) {
    log.warn("Unsupported media type: {}", exception.getMessage());
    String contentType = exception.getContentType() == null
        ? "unknown"
        : exception.getContentType().toString();
    String message = UNSUPPORTED_FORMAT_MESSAGE.formatted(contentType);
    return errorResponse(HttpStatus.BAD_REQUEST,
        new ErrorResponse(message, VALIDATION_ERROR_CODE, null));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
    log.warn("Not found: {}", exception.getMessage());
    String message = normalizeNotFoundMessage(exception.getMessage());
    return errorResponse(HttpStatus.NOT_FOUND,
        new ErrorResponse(message, NOT_FOUND_ERROR_CODE, null));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ErrorResponse> handleValidationErrors(Exception exception) {
    logValidationWarning(exception.getMessage());
    Map<String, String> details = new LinkedHashMap<>();
    if (exception instanceof MethodArgumentNotValidException invalidException) {
      collectFieldErrors(invalidException.getBindingResult(), details);
    } else if (exception instanceof BindException bindException) {
      collectFieldErrors(bindException.getBindingResult(), details);
    }
    return validationErrorResponse(details);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException exception) {
    log.warn("Type mismatch error: {}", exception.getMessage());
    String value = exception.getValue() == null ? "null" : exception.getValue().toString();
    String errorMessage = "Invalid value '" + value + "' for ID. Must be a positive integer";
    return errorResponse(HttpStatus.BAD_REQUEST,
        new ErrorResponse(errorMessage, VALIDATION_ERROR_CODE, null));
  }

  @ExceptionHandler({MissingServletRequestParameterException.class,
      ConstraintViolationException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponse> handleRequestValidation(Exception exception) {
    log.warn("Request validation error: {}", exception.getMessage());
    Map<String, String> details = new LinkedHashMap<>();
    if (exception instanceof MissingServletRequestParameterException missingException) {
      details.put(missingException.getParameterName(), missingException.getMessage());
    } else if (exception instanceof IllegalArgumentException illegalArgumentException) {
      details.put("request", illegalArgumentException.getMessage());
    }
    if (exception instanceof ConstraintViolationException constraintViolationException) {
      for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
        String path =
            violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        details.put(field.isBlank() ? "request" : field, violation.getMessage());
      }
    }
    return validationErrorResponse(
        details.isEmpty() ? Map.of("request", exception.getMessage()) : details);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericError(Exception exception) {
    log.error("Unhandled exception", exception);
    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        new ErrorResponse(INTERNAL_ERROR_MESSAGE, INTERNAL_ERROR_CODE, null));
  }

  private ResponseEntity<ErrorResponse> validationErrorResponse(Map<String, String> details) {
    Map<String, String> responseDetails = details == null || details.isEmpty() ? null : details;
    return errorResponse(HttpStatus.BAD_REQUEST,
        new ErrorResponse(VALIDATION_ERROR_MESSAGE, VALIDATION_ERROR_CODE, responseDetails));
  }

  private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, ErrorResponse body) {
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  private void logValidationWarning(String message) {
    log.warn("Validation error: {}", message);
  }

  private void collectFieldErrors(BindingResult bindingResult, Map<String, String> details) {
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      details.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
  }

  private String normalizeNotFoundMessage(String message) {
    if (message == null) {
      return null;
    }
    return message.replace("Resource with id=", "Resource with ID=");
  }
}

