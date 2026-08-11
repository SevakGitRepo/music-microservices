package com.epam.songservice.song.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SongMetadataResponse {

  private Long id;
  private String name;
  private String artist;
  private String album;
  private String duration;
  private String year;

}

