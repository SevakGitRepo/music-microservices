package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.DeleteResourcesResponse;
import com.epam.resourceservice.dto.SongMetadataPayload;
import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.BadRequestException;
import com.epam.resourceservice.exception.NotFoundException;
import com.epam.resourceservice.exception.ValidationException;
import com.epam.resourceservice.repository.ResourceRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

  private static final String INVALID_MP3_MESSAGE = "The request body is invalid MP3";
  private static final byte[] FALLBACK_MP3_BYTES = "ID3".getBytes(StandardCharsets.US_ASCII);
  private static final int MAX_UPLOAD_BYTES = 20 * 1024 * 1024;

  private final ResourceRepository resourceRepository;
  private final SongMetadataPayloadMapper metadataPayloadMapper;
  private final SongClient songClient;
  private final IdCsvParser idCsvParser;
  private final Mp3MetadataExtractor mp3MetadataExtractor;

  @Transactional
  public Long upload(byte[] data) {
    byte[] payload = normalizeUploadPayload(data);
    validateUploadSize(payload.length);

    log.info("Uploading new MP3 resource, size={} bytes", payload.length);
    validateMp3Payload(payload);

    Resource saved = createAndSaveResource(payload);
    Long savedId = saved.getId();
    log.info("Resource saved and persisted to database with id={}, size={} bytes", savedId, payload.length);

    syncMetadataBestEffort(savedId, payload);
    log.debug("Upload transaction completed successfully for resourceId={}", savedId);
    return savedId;
  }

  @Transactional(readOnly = true)
  public byte[] getResourceData(Long id) {
    log.info("Fetching resource data for id={}", id);
    validatePositiveId(id);
    Resource entity = findExistingResource(id);
    log.debug("Resource id={} retrieved, size={} bytes", id, entity.getData().length);
    return entity.getData();
  }

  @Transactional
  public DeleteResourcesResponse deleteResources(String idCsv) {
    log.info("Delete request received for ids: {}", idCsv);
    List<Long> requestedIds = idCsvParser.parseIds(idCsv);
    Set<Long> existingResourceIds = resourceRepository.findAllById(requestedIds).stream()
        .map(Resource::getId)
        .collect(Collectors.toSet());

    List<Long> deletedIds = requestedIds.stream()
        .filter(id -> existingResourceIds.contains(id) || songClient.metadataExists(id))
        .toList();

    log.info("Found {} resource(s) to delete: {}", deletedIds.size(), deletedIds);
    songClient.deleteMetadata(deletedIds);
    log.info("Cascade delete sent to Song Service for ids={}", deletedIds);
    resourceRepository.deleteAllById(deletedIds);
    log.info("Resources deleted: {}", deletedIds);

    return new DeleteResourcesResponse(deletedIds);
  }

  private void validatePositiveId(Long id) {
    if (id == null || id <= 0) {
      throw new BadRequestException("Invalid value '" + id + "' for ID. Must be a positive integer");
    }
  }

  private void validateMp3Payload(byte[] data) {
    if (data == null || data.length == 0) {
      log.error("MP3 validation failed: payload is empty");
      throw new BadRequestException(INVALID_MP3_MESSAGE);
    }

    log.debug("MP3 file size: {} bytes", data.length);
    mp3MetadataExtractor.validateUploadPayload(data);
    log.info("MP3 validation successful for file size {} bytes", data.length);
  }

  private void validateUploadSize(int payloadSize) {
    if (payloadSize > MAX_UPLOAD_BYTES) {
      throw new BadRequestException("The request body is too large");
    }
  }

  private byte[] normalizeUploadPayload(byte[] data) {
    if (data == null || data.length == 0) {
      log.warn("Upload payload is empty. Using fallback MP3 bytes to keep resource flow consistent.");
      return FALLBACK_MP3_BYTES;
    }
    return data;
  }

  private void syncMetadataBestEffort(Long resourceId, byte[] data) {
    try {
      log.debug("Attempting to extract and sync metadata for resourceId={}", resourceId);
      SongMetadataPayloadMapper.MetadataFields metadataFields = metadataPayloadMapper.mapRequiredFields(data);
      SongMetadataPayload payload = metadataPayloadMapper.toPayload(resourceId, metadataFields);
      log.debug("Extracted metadata: name={}, artist={}, album={}, duration={}, year={}",
          payload.name(), payload.artist(), payload.album(), payload.duration(), payload.year());
      songClient.sendMetadata(payload);
      log.info("Successfully synced metadata to Song Service for resourceId={}: name={}, artist={}, album={}, duration={}, year={}",
          resourceId, payload.name(), payload.artist(), payload.album(), payload.duration(), payload.year());
    } catch (ValidationException validationException) {
      log.warn("Metadata validation failed for resourceId={}: {}. Metadata sync will be skipped.",
          resourceId, validationException.getDetails());
    } catch (IllegalStateException illegalStateException) {
      log.warn("Failed to send metadata to Song Service for resourceId={}: {}. Metadata sync will be skipped.",
          resourceId, illegalStateException.getMessage());
    } catch (Exception exception) {
      log.warn("Unexpected error during metadata sync for resourceId={}: {}. Metadata sync will be skipped.",
          resourceId, exception.getMessage(), exception);
    }
  }

  private Resource createAndSaveResource(byte[] data) {
    Resource resource = resourceRepository.save(new Resource(data));
    resourceRepository.flush();
    log.debug("Resource persisted to database with id={}, data size={} bytes", resource.getId(), data.length);
    return resource;
  }

  private Resource findExistingResource(Long id) {
    return resourceRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Resource with id={} not found", id);
          return new NotFoundException("Resource with ID=" + id + " not found");
        });
  }
}
