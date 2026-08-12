package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.SongMetadataPayload;
import com.epam.resourceservice.exception.ValidationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongMetadataPayloadMapper {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_ARTIST = "artist";
  private static final String FIELD_ALBUM = "album";
  private static final String FIELD_DURATION = "duration";
  private static final String FIELD_YEAR = "year";

  private static final String NAME_LENGTH_MESSAGE = "Name must be between 1 and 100 characters";
  private static final String ARTIST_LENGTH_MESSAGE = "Artist must be between 1 and 100 characters";
  private static final String ALBUM_LENGTH_MESSAGE = "Album must be between 1 and 100 characters";
  private static final String DURATION_FORMAT_MESSAGE = "Duration must be in mm:ss format with leading zeros";
  private static final String YEAR_RANGE_MESSAGE = "Year must be between 1900 and 2099";

  private static final Pattern DURATION_PATTERN = Pattern.compile("^[0-5]\\d:[0-5]\\d$");
  private static final Pattern YEAR_PATTERN = Pattern.compile("^(19\\d{2}|20\\d{2})$");

  private static final List<String> NAME_KEYS = List.of("title", "dc:title", "xmpDM:title", "Title");
  private static final List<String> ARTIST_KEYS = List.of("xmpDM:artist", "artist", "Author", "dc:creator");
  private static final List<String> ALBUM_KEYS = List.of("xmpDM:album", "album", "xmpDM:releaseName");
  private static final List<String> DURATION_KEYS = List.of("duration", "xmpDM:duration");
  private static final List<String> YEAR_KEYS = List.of("xmpDM:releaseDate", "xmpDM:releaseYear", "year", "xmpDM:year", "Year");

  private final Mp3MetadataExtractor metadataExtractor;

   public MetadataFields mapRequiredFields(byte[] data) {
     Map<String, String> metadata = metadataExtractor.extractMetadata(data);
     log.debug("Extracted metadata: {}", metadata);

     String name = getValue(metadata, NAME_KEYS);
     String artist = getValue(metadata, ARTIST_KEYS);
     String album = getValue(metadata, ALBUM_KEYS);
     String duration = getValue(metadata, DURATION_KEYS);
     String year = getValue(metadata, YEAR_KEYS);

     name = (name != null && !name.isBlank()) ? name : "Unknown Title";
     artist = (artist != null && !artist.isBlank()) ? artist : "Unknown Artist";
     album = (album != null && !album.isBlank()) ? album : "Unknown Album";
     duration = (duration != null && !duration.isBlank()) ? duration : "00:00";
     year = (year != null && !year.isBlank()) ? year : "1900";

     log.debug("Metadata after defaults applied: name={}, artist={}, album={}, duration={}, year={}",
         name, artist, album, duration, year);

     Map<String, String> details = new LinkedHashMap<>();
     validateLength(name, FIELD_NAME, NAME_LENGTH_MESSAGE, details);
     validateLength(artist, FIELD_ARTIST, ARTIST_LENGTH_MESSAGE, details);
     validateLength(album, FIELD_ALBUM, ALBUM_LENGTH_MESSAGE, details);
     validateDuration(duration, details);
     validateYear(year, details);

     if (!details.isEmpty()) {
       log.warn("Validation errors for metadata: {}", details);
       throw new ValidationException(details);
     }

     return new MetadataFields(name, artist, album, duration, year);
   }

  public SongMetadataPayload toPayload(Long resourceId, MetadataFields fields) {
    return new SongMetadataPayload(
        resourceId,
        fields.name(),
        fields.artist(),
        fields.album(),
        fields.duration(),
        fields.year()
    );
  }

  private String getValue(Map<String, String> metadata, List<String> keys) {
    for (String key : keys) {
      String value = metadata.get(key);
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

   private void validateLength(String value, String fieldName, String message, Map<String, String> details) {
     if (value != null && !value.isBlank() && value.length() > 100) {
       details.put(fieldName, message);
     }
   }

   private void validateDuration(String duration, Map<String, String> details) {
     if (duration != null && !DURATION_PATTERN.matcher(duration).matches()) {
       details.put(FIELD_DURATION, DURATION_FORMAT_MESSAGE);
     }
   }

   private void validateYear(String year, Map<String, String> details) {
     if (year != null && !YEAR_PATTERN.matcher(year).matches()) {
       details.put(FIELD_YEAR, YEAR_RANGE_MESSAGE);
     }
   }


  public record MetadataFields(
      String name,
      String artist,
      String album,
      String duration,
      String year
  ) {
  }
}
