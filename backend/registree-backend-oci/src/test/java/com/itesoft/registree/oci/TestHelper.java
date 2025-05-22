package com.itesoft.registree.oci;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.itesoft.registree.oci.dto.json.CatalogDto;
import com.itesoft.registree.oci.dto.json.RepositoryTagsDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;

abstract class TestHelper {
  public static final String CATALOG_URI = "%s/v2/_catalog";
  public static final String LIST_TAGS_URI = "%s/v2/%s/tags/list";

  public static List<String> getRepositories(final ObjectMapper objectMapper,
                                             final String baseUrl)
    throws Exception {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      final URIBuilder uriBuilder =
        new URIBuilder(String.format(CATALOG_URI,
                                     baseUrl));
      final HttpGet httpGet = new HttpGet(uriBuilder.build());
      return httpClient.execute(httpGet, response -> {
        assertEquals(HttpStatus.SC_OK, response.getCode());
        final HttpEntity entity = response.getEntity();
        final CatalogDto catalogDto =
          objectMapper.readValue(entity.getContent(),
                                 CatalogDto.class);
        return catalogDto.getRepositories();
      });
    }
  }

  public static List<String> getTags(final ObjectMapper objectMapper,
                                     final String baseUrl,
                                     final String name)
    throws Exception {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      final URIBuilder uriBuilder =
        new URIBuilder(String.format(LIST_TAGS_URI,
                                     baseUrl,
                                     name));
      final HttpGet httpGet = new HttpGet(uriBuilder.build());
      return httpClient.execute(httpGet, response -> {
        assertEquals(HttpStatus.SC_OK, response.getCode());
        final HttpEntity entity = response.getEntity();
        final RepositoryTagsDto repositorytagsDto =
          objectMapper.readValue(entity.getContent(),
                                 RepositoryTagsDto.class);
        return repositorytagsDto.getTags();
      });
    }
  }

}
