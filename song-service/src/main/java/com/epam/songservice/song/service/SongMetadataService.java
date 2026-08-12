package com.epam.songservice.song.service;

import com.epam.songservice.song.entity.SongMetadata;
import com.epam.songservice.song.dto.CreateSongMetadataRequest;
import com.epam.songservice.song.dto.SongMetadataResponse;
import com.epam.songservice.song.exception.SongMetadataConflictException;
import com.epam.songservice.song.exception.SongMetadataNotFoundException;
import com.epam.songservice.song.repository.SongMetadataRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Slf4j
public class SongMetadataService {

  private static final String METADATA_CONFLICT_MESSAGE = "Metadata for resource ID=%d already exists";
  private static final String METADATA_NOT_FOUND_MESSAGE = "Song metadata for ID=%d not found";

  private final SongMetadataRepository repository;
  private final IdCsvParser idCsvParser;

  @Transactional
  public Long create(CreateSongMetadataRequest request) {
    log.info("Creating song metadata for resourceId={}", request.getId());
    if (repository.existsById(request.getId())) {
      log.warn("Metadata already exists for id={}", request.getId());
      throw new SongMetadataConflictException(String.format(METADATA_CONFLICT_MESSAGE, request.getId()));
    }

    SongMetadata entity = toEntity(request);

    Long savedId = repository.save(entity).getId();
    log.info("Song metadata created with id={}", savedId);
    return savedId;
  }

  @Transactional(readOnly = true)
  public SongMetadataResponse getById(Long id) {
    log.info("Fetching song metadata for id={}", id);
    SongMetadata entity = repository.findById(id)
        .orElseThrow(() -> {
          log.warn("Song metadata not found for id={}", id);
          return new SongMetadataNotFoundException(String.format(METADATA_NOT_FOUND_MESSAGE, id));
        });

    SongMetadataResponse response = toResponse(entity);
    log.debug("Song metadata retrieved: {}", response);
    return response;
  }

  @Transactional
  public List<Long> deleteByIds(List<Long> ids) {
    log.info("Deleting song metadata for ids={}", ids);
    List<Long> deletedIds = repository.findAllById(ids).stream()
        .map(SongMetadata::getId)
        .toList();
    repository.deleteAllById(deletedIds);
    repository.flush();
    log.info("Deleted song metadata ids={}", deletedIds);
    return deletedIds;
  }

  @Transactional
  public List<Long> deleteByCsv(String idsCsv) {
    return deleteByIds(idCsvParser.parseIds(idsCsv));
  }

  private SongMetadata toEntity(CreateSongMetadataRequest request) {
    SongMetadata entity = new SongMetadata();
    entity.setId(request.getId());
    entity.setName(request.getName());
    entity.setArtist(request.getArtist());
    entity.setAlbum(request.getAlbum());
    entity.setDuration(request.getDuration());
    entity.setYear(request.getYear());
    return entity;
  }

  private SongMetadataResponse toResponse(SongMetadata entity) {
    SongMetadataResponse response = new SongMetadataResponse();
    response.setId(entity.getId());
    response.setName(entity.getName());
    response.setArtist(entity.getArtist());
    response.setAlbum(entity.getAlbum());
    response.setDuration(entity.getDuration());
    response.setYear(entity.getYear());
    return response;
  }
}

