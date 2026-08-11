package com.epam.songservice.song.exception;

public class SongMetadataNotFoundException extends RuntimeException {

  public SongMetadataNotFoundException(String message) {
    super(message);
  }
}

