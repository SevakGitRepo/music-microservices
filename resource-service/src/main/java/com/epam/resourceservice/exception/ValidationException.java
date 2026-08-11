package com.epam.resourceservice.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

  private final Map<String, String> details;

  public ValidationException(Map<String, String> details) {
    super("Validation error");
    this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
  }

}

