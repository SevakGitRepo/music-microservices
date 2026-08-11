package com.epam.songservice.song.controller;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response",
	example = "{\"errorMessage\":\"An error occurred on the server\"}")
public record ApiErrorResponse(String errorMessage) {

}

