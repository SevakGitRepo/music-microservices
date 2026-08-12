package com.epam.songservice.song.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IdCsvParser {

  private static final String CSV_DELIMITER = ",";

  public List<Long> parseIds(String csv) {
    return Arrays.stream(csv.split(CSV_DELIMITER))
        .map(Long::parseLong)
        .toList();
  }
}

