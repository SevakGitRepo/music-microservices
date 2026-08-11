package com.epam.resourceservice.service;

import com.epam.resourceservice.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Component;
import org.xml.sax.helpers.DefaultHandler;

@Component
@RequiredArgsConstructor
public class Mp3MetadataExtractor {

  private static final String MP3_MIME = "audio/mpeg";
  private static final String INVALID_MP3_MESSAGE = "The request body is invalid MP3";
  private static final String XMP_DURATION_KEY = "xmpDM:duration";
  private static final String DURATION_KEY = "duration";

  private final Tika tika;
  private final AutoDetectParser parser;


  public Map<String, String> extractMetadata(byte[] bytes) {
    String detectedMime = tika.detect(bytes);
    if (!MP3_MIME.equals(detectedMime)) {
      throw invalidMp3Exception();
    }

    Metadata metadata = parseMetadata(bytes);

    Map<String, String> extracted = new LinkedHashMap<>();
    for (String name : metadata.names()) {
      extracted.put(name, metadata.get(name));
    }

    convertDurationIfPresent(extracted, XMP_DURATION_KEY);
    convertDurationIfPresent(extracted, DURATION_KEY);
    return extracted;
  }

  private Metadata parseMetadata(byte[] bytes) {
    Metadata metadata = new Metadata();
    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
      parser.parse(inputStream, new DefaultHandler(), metadata, new ParseContext());
      return metadata;
    } catch (Exception exception) {
      throw invalidMp3Exception();
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
    } catch (NumberFormatException ignored) {
      // Keep original value untouched when duration cannot be parsed.
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

