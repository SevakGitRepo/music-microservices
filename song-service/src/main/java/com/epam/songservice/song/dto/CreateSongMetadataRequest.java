package com.epam.songservice.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateSongMetadataRequest {

  private static final String DURATION_PATTERN = "^[0-5]\\d:[0-5]\\d$";
  private static final String YEAR_PATTERN = "^(19\\d{2}|20\\d{2})$";

  @NotNull
  @Positive
  private Long id;

  @NotBlank
  @Size(min = 1, max = 100)
  private String name;

  @NotBlank
  @Size(min = 1, max = 100)
  private String artist;

  @NotBlank
  @Size(min = 1, max = 100)
  private String album;

  @NotBlank
  @Pattern(regexp = DURATION_PATTERN, message = "duration must match mm:ss")
  private String duration;

  @NotBlank
  @Pattern(regexp = YEAR_PATTERN, message = "year must be between 1900 and 2099")
  private String year;

}

