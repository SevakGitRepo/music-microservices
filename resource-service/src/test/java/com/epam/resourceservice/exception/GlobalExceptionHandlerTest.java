package com.epam.resourceservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.resourceservice.dto.ErrorResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private static final String DURATION_ERROR = "Duration must be in mm:ss format with leading zeros";
  private static final String YEAR_ERROR = "Year must be between 1900 and 2099";

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldReturnStructuredValidationError() {
    ValidationException exception = new ValidationException(Map.of(
        "duration", DURATION_ERROR,
        "year", YEAR_ERROR
    ));

    ResponseEntity<ErrorResponse> response = handler.handleStructuredValidation(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(response.getBody()).isEqualTo(new ErrorResponse(
        "Validation error",
        "400",
        Map.of(
            "duration", DURATION_ERROR,
            "year", YEAR_ERROR
        )
    ));
  }

  @Test
  void shouldNormalizeBadRequestToValidationEnvelope() {
    ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
        new BadRequestException("Missing required MP3 tag for field: duration"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(response.getBody()).isEqualTo(new ErrorResponse(
        "Validation error",
        "400",
        Map.of("request", "Missing required MP3 tag for field: duration")
    ));
  }

  @Test
  void shouldReturnNotFoundResponse() {
    ResponseEntity<ErrorResponse> response = handler.handleNotFound(
        new NotFoundException("Resource with id=1 not found"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(response.getBody()).isEqualTo(new ErrorResponse(
        "Resource with id=1 not found",
        "404",
        null
    ));
  }
}

