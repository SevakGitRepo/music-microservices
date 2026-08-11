package com.epam.songservice.song.controller;

import com.epam.songservice.song.dto.CreateSongMetadataRequest;
import com.epam.songservice.song.dto.CreateSongMetadataResponse;
import com.epam.songservice.song.dto.DeleteSongMetadataResponse;
import com.epam.songservice.song.dto.SongMetadataResponse;
import com.epam.songservice.song.service.SongMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/songs")
@Validated
@Tag(name = "Song Metadata", description = "CRUD operations for song metadata")
public class SongMetadataController {

  private static final String ID_CSV_PATTERN = "^\\d+(,\\d+)*$";
  private static final String ID_CSV_DELIMITER = ",";

  private final SongMetadataService service;

  public SongMetadataController(SongMetadataService service) {
    this.service = service;
  }

  @Operation(summary = "Create song metadata", description = "Creates metadata for an uploaded song resource")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Song metadata created successfully",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateSongMetadataResponse.class))),
      @ApiResponse(responseCode = "400", description = "Request payload is invalid",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Referenced resource does not exist",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "An error occurred on the server",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @PostMapping
  public CreateSongMetadataResponse create(@Valid @RequestBody CreateSongMetadataRequest request) {
    return new CreateSongMetadataResponse(service.create(request));
  }

  @Operation(summary = "Get song metadata", description = "Retrieves song metadata by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Song metadata retrieved successfully",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = SongMetadataResponse.class))),
      @ApiResponse(responseCode = "400", description = "The provided ID is invalid",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Song metadata with the specified ID does not exist",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "An error occurred on the server",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public SongMetadataResponse getById(
      @Parameter(description = "The ID of the song metadata to retrieve", example = "1")
      @PathVariable @Positive Long id) {
    return service.getById(id);
  }

  @Operation(summary = "Delete song metadata",
      description = "Deletes song metadata entries by comma-separated IDs")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Request successful, metadata deleted as specified",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DeleteSongMetadataResponse.class))),
      @ApiResponse(responseCode = "400", description = "CSV string format is invalid or exceeds length restrictions",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "An error occurred on the server",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @DeleteMapping
  public DeleteSongMetadataResponse delete(
      @Parameter(description = "Comma-separated list of song metadata IDs to delete", example = "1,2")
      @RequestParam("id")
      @Size(max = 200)
      @Pattern(regexp = ID_CSV_PATTERN, message = "id must be a comma-separated list of positive integers")
      String id
  ) {
    return new DeleteSongMetadataResponse(service.deleteByIds(parseCsvIds(id)));
  }

  private List<Long> parseCsvIds(String csvIds) {
    return Arrays.stream(csvIds.split(ID_CSV_DELIMITER))
        .map(Long::parseLong)
        .toList();
  }
}

