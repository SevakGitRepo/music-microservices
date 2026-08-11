package com.epam.resourceservice.config;

import org.apache.tika.Tika;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

  @Bean
  public RestClient restClient(
      @Value("${song.service.base-url:http://localhost:8081}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  public Tika tika() {
    return new Tika();
  }

  @Bean
  public AutoDetectParser autoDetectParser() {
    return new AutoDetectParser();
  }
}

