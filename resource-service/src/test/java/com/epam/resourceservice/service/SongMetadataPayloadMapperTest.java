package com.epam.resourceservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.epam.resourceservice.exception.ValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SongMetadataPayloadMapperTest {

  private static final String DURATION_ERROR = "Duration must be in mm:ss format with leading zeros";
  private static final String YEAR_ERROR = "Year must be between 1900 and 2099";

  @Mock
  private Mp3MetadataExtractor metadataExtractor;

  @InjectMocks
  private SongMetadataPayloadMapper mapper;

  @Test
  void shouldReturnValidationErrorForInvalidDurationAndYear() {
    when(metadataExtractor.extractMetadata(new byte[] {1})).thenReturn(Map.of(
        "title", "Track",
        "artist", "Artist",
        "album", "Album",
        "duration", "1:2",
        "year", "1899"
    ));

    assertThatThrownBy(() -> mapper.mapRequiredFields(new byte[] {1}))
        .isInstanceOf(ValidationException.class)
        .isInstanceOfSatisfying(ValidationException.class, exception -> assertThat(exception.getDetails())
            .containsEntry("duration", DURATION_ERROR)
            .containsEntry("year", YEAR_ERROR));
  }
}

