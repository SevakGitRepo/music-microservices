package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.DeleteResourcesResponse;
import com.epam.resourceservice.dto.SongMetadataPayload;
import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.BadRequestException;
import com.epam.resourceservice.exception.NotFoundException;
import com.epam.resourceservice.repository.ResourceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

  private static final String INVALID_MP3_MESSAGE = "The request body is invalid MP3";
  private static final String INVALID_ID_MESSAGE = "id must be a positive integer";

  private final ResourceRepository resourceRepository;
  private final SongMetadataPayloadMapper metadataPayloadMapper;
  private final SongClient songClient;
  private final IdCsvParser idCsvParser;

  @Transactional
  public Long upload(byte[] data) {
    log.info("Uploading new MP3 resource, size={} bytes", data == null ? 0 : data.length);
    validateMp3Payload(data);

    SongMetadataPayloadMapper.MetadataFields metadataFields = metadataPayloadMapper.mapRequiredFields(data);

    Resource saved = createAndSaveResource(data);
    log.info("Resource saved with id={}", saved.getId());

    SongMetadataPayload payload = metadataPayloadMapper.toPayload(saved.getId(), metadataFields);
    songClient.sendMetadata(payload);
    log.info("Metadata sent to Song Service for resourceId={}", saved.getId());
    return saved.getId();
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
    List<Resource> existing = resourceRepository.findAllById(requestedIds);
    List<Long> deletedIds = existing.stream()
        .map(Resource::getId)
        .toList();

    log.info("Found {} resource(s) to delete: {}", deletedIds.size(), deletedIds);
    songClient.deleteMetadata(deletedIds);
    log.info("Cascade delete sent to Song Service for ids={}", deletedIds);
    resourceRepository.deleteAll(existing);
    log.info("Resources deleted: {}", deletedIds);

    return new DeleteResourcesResponse(deletedIds);
  }

  private void validatePositiveId(Long id) {
    if (id == null || id <= 0) {
      throw new BadRequestException(INVALID_ID_MESSAGE);
    }
  }

  private void validateMp3Payload(byte[] data) {
    if (data == null || data.length == 0) {
      throw new BadRequestException(INVALID_MP3_MESSAGE);
    }
  }

  private Resource createAndSaveResource(byte[] data) {
    return resourceRepository.save(new Resource(data));
  }

  private Resource findExistingResource(Long id) {
    return resourceRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Resource with id={} not found", id);
          return new NotFoundException("Resource with id=" + id + " not found");
        });
  }
}
