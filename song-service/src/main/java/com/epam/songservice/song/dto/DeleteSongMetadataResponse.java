package com.epam.songservice.song.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after deleting song metadata entries")
public record DeleteSongMetadataResponse(List<Long> ids) {

}

