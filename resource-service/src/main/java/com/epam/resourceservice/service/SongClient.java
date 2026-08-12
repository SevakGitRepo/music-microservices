package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.SongMetadataPayload;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class SongClient {

  private static final String ID_CSV_DELIMITER = ",";
  private static final String SEND_METADATA_ERROR_MESSAGE = "Failed to send metadata to Song Service";
  private static final String DELETE_METADATA_ERROR_MESSAGE = "Failed to delete metadata in Song Service";

  private final RestClient restClient;
  private final String metadataEndpoint;

  public SongClient(RestClient restClient,
      @Value("${song.service.metadata-endpoint:/songs}") String metadataEndpoint) {
    this.restClient = restClient;
    this.metadataEndpoint = metadataEndpoint;
  }

  public void sendMetadata(SongMetadataPayload payload) {
    log.info("Sending metadata to Song Service for resourceId={}", payload.id());
    executeWithErrorHandling(() -> restClient.post()
            .uri(metadataEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toBodilessEntity(),
        SEND_METADATA_ERROR_MESSAGE,
        exception -> log.error("Failed to send metadata to Song Service for resourceId={}", payload.id(), exception),
        () -> log.debug("Metadata successfully sent for resourceId={}", payload.id()));
  }

  public void deleteMetadata(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      log.debug("No metadata IDs to delete, skipping Song Service call");
      return;
    }

    String idParam = toCsv(ids);

    log.info("Requesting metadata deletion in Song Service for ids={}", ids);
    executeWithErrorHandling(() -> restClient.delete()
            .uri(uriBuilder -> uriBuilder.path(metadataEndpoint).queryParam("id", idParam).build())
            .retrieve()
            .toBodilessEntity(),
        DELETE_METADATA_ERROR_MESSAGE,
        exception -> log.error("Failed to delete metadata in Song Service for ids={}", ids, exception),
        () -> log.debug("Metadata deletion confirmed in Song Service for ids={}", ids));
  }

  public boolean metadataExists(Long id) {
    try {
      restClient.get()
          .uri(uriBuilder -> uriBuilder.path(metadataEndpoint + "/{id}").build(id))
          .retrieve()
          .toBodilessEntity();
      return true;
    } catch (HttpClientErrorException.NotFound exception) {
      return false;
    } catch (RestClientException exception) {
      log.warn("Failed to verify Song Service metadata existence for id={}", id, exception);
      return false;
    }
  }

  private String toCsv(List<Long> ids) {
    return ids.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(ID_CSV_DELIMITER));
  }

  private void executeWithErrorHandling(Runnable action,
      String errorMessage,
      Consumer<RestClientException> onFailureLog,
      Runnable onSuccessLog) {
    try {
      action.run();
      onSuccessLog.run();
    } catch (RestClientException exception) {
      onFailureLog.accept(exception);
      throw new IllegalStateException(errorMessage, exception);
    }
  }
}
