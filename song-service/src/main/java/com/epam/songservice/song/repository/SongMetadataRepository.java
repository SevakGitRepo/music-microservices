package com.epam.songservice.song.repository;

import com.epam.songservice.song.entity.SongMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongMetadataRepository extends JpaRepository<SongMetadata, Long> {
}

