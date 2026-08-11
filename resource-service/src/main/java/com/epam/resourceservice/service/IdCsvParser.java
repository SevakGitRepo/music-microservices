package com.epam.resourceservice.service;

import com.epam.resourceservice.exception.BadRequestException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class IdCsvParser {

  private static final int MAX_CSV_LENGTH = 200;
  private static final String CSV_DELIMITER = ",";
  private static final Pattern CSV_PATTERN = Pattern.compile("^\\d+(,\\d+)*$");
  private static final String REQUIRED_IDS_MESSAGE = "id query parameter is required";
  private static final String MAX_LENGTH_MESSAGE = "id CSV string length must not exceed 200 characters";
  private static final String INVALID_FORMAT_MESSAGE = "id CSV string format is invalid";
  private static final String NON_POSITIVE_ID_MESSAGE = "id values must be positive integers";

  public List<Long> parseIds(String csv) {
    validateCsv(csv);

    try {
      return parseDistinctPositiveIds(csv);
    } catch (NumberFormatException exception) {
      throw new BadRequestException(INVALID_FORMAT_MESSAGE);
    }
  }

  private void validateCsv(String csv) {
    if (csv == null || csv.isBlank()) {
      throw new BadRequestException(REQUIRED_IDS_MESSAGE);
    }
    if (csv.length() > MAX_CSV_LENGTH) {
      throw new BadRequestException(MAX_LENGTH_MESSAGE);
    }
    if (!CSV_PATTERN.matcher(csv).matches()) {
      throw new BadRequestException(INVALID_FORMAT_MESSAGE);
    }
  }

  private List<Long> parseDistinctPositiveIds(String csv) {
    return Arrays.stream(csv.split(CSV_DELIMITER))
        .map(Long::parseLong)
        .peek(id -> {
          if (id <= 0) {
            throw new BadRequestException(NON_POSITIVE_ID_MESSAGE);
          }
        })
        .distinct()
        .toList();
  }
}

