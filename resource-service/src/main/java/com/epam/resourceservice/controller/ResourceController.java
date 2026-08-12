package com.epam.resourceservice.controller;

import com.epam.resourceservice.dto.DeleteResourcesResponse;
import com.epam.resourceservice.dto.UploadResourceResponse;
import com.epam.resourceservice.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resources")
@Tag(name = "Resources", description = "CRUD operations for MP3 resources")
public class ResourceController {

  private static final String AUDIO_MPEG = "audio/mpeg";

  private final ResourceService resourceService;

  @Operation(summary = "Upload resource", description = "Uploads a new MP3 resource")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resource uploaded successfully",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = UploadResourceResponse.class))),
      @ApiResponse(responseCode = "400", description = "The request body is invalid MP3", content = @Content),
      @ApiResponse(responseCode = "500", description = "An error occurred on the server", content = @Content)
  })
  @PostMapping(consumes = AUDIO_MPEG, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UploadResourceResponse> upload(
      @RequestBody(required = false) byte[] mp3Data) {
    Long id = resourceService.upload(mp3Data);
    return ResponseEntity.ok(new UploadResourceResponse(id));
  }

  @Operation(summary = "Get resource", description = "Retrieves the binary audio data of a resource")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resource retrieved successfully",
          content = @Content(mediaType = AUDIO_MPEG,
              schema = @Schema(type = "string", format = "binary"))),
      @ApiResponse(responseCode = "400", description = "The provided ID is invalid", content = @Content),
      @ApiResponse(responseCode = "404", description = "Resource with the specified ID does not exist", content = @Content),
      @ApiResponse(responseCode = "500", description = "An error occurred on the server", content = @Content)
  })
  @GetMapping(value = "/{id}")
  public ResponseEntity<byte[]> getResource(
      @Parameter(description = "The ID of the resource to retrieve", example = "1")
      @PathVariable Long id) {
    byte[] content = resourceService.getResourceData(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(AUDIO_MPEG))
        .body(content);
  }

  @Operation(summary = "Delete resources",
      description = "Deletes specified resources by their comma-separated IDs")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Request successful, resources deleted as specified",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DeleteResourcesResponse.class))),
      @ApiResponse(responseCode = "400", description = "CSV string format is invalid or exceeds length restrictions", content = @Content),
      @ApiResponse(responseCode = "500", description = "An error occurred on the server", content = @Content)
  })
  @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DeleteResourcesResponse> deleteResources(
      @Parameter(description = "Comma-separated list of resource IDs to delete", example = "1,2")
      @RequestParam("id") String idCsv) {
    return ResponseEntity.ok(resourceService.deleteResources(idCsv));
  }
}
