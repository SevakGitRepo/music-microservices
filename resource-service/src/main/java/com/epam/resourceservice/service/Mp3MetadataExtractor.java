package com.epam.resourceservice.service;

import com.epam.resourceservice.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Component;
import org.xml.sax.helpers.DefaultHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class Mp3MetadataExtractor {

  private static final String INVALID_MP3_MESSAGE = "The request body is invalid MP3";
  private static final String XMP_DURATION_KEY = "xmpDM:duration";
  private static final String DURATION_KEY = "duration";

  private final AutoDetectParser parser;


  public Map<String, String> extractMetadata(byte[] bytes) {
    validateUploadPayload(bytes);

    Metadata metadata = parseMetadataOrEmpty(bytes);

    Map<String, String> extracted = new LinkedHashMap<>();
    for (String name : metadata.names()) {
      extracted.put(name, metadata.get(name));
    }

    convertDurationIfPresent(extracted, XMP_DURATION_KEY);
    convertDurationIfPresent(extracted, DURATION_KEY);
    return extracted;
  }

  public void validateUploadPayload(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      log.warn("MP3 validation failed: payload is empty");
      throw invalidMp3Exception();
    }

    log.debug("Upload payload accepted, size={} bytes", bytes.length);
  }

  private Metadata parseMetadataOrEmpty(byte[] bytes) {
    Metadata metadata = new Metadata();
    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
      parser.parse(inputStream, new DefaultHandler(), metadata, new ParseContext());
      return metadata;
    } catch (Exception exception) {

      log.warn("MP3 metadata parsing failed, continuing without extracted tags", exception);
      return metadata;
    }
  }

  private void convertDurationIfPresent(Map<String, String> metadata, String durationKey) {
    String rawDuration = metadata.get(durationKey);
    if (rawDuration == null || rawDuration.isBlank()) {
      return;
    }

    try {
      double seconds = Double.parseDouble(rawDuration.trim());
      metadata.put(durationKey, toMmSs(seconds));
    } catch (NumberFormatException exception) {
      log.debug("Could not parse duration '{}' for key '{}': {}", rawDuration, durationKey, exception.getMessage());
    }
  }

  private String toMmSs(double seconds) {
    int roundedSeconds = (int) Math.round(seconds);
    return String.format("%02d:%02d", roundedSeconds / 60, roundedSeconds % 60);
  }

  private BadRequestException invalidMp3Exception() {
    return new BadRequestException(INVALID_MP3_MESSAGE);
  }
}

