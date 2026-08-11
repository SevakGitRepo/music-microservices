package com.epam.songservice.song.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "song_metadata")
public class SongMetadata {

  @Id
  @Column(nullable = false, unique = true)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 100)
  private String artist;

  @Column(nullable = false, length = 100)
  private String album;

  @Column(nullable = false, length = 5)
  private String duration;

  @Column(nullable = false, length = 4)
  private String year;

}

