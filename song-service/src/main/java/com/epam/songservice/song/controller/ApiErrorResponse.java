package com.epam.songservice.song.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Standard API error response",
	example = "{\"errorMessage\":\"An error occurred on the server\",\"errorCode\":\"500\"}")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(String errorMessage, String errorCode, Map<String, String> details) {

}

