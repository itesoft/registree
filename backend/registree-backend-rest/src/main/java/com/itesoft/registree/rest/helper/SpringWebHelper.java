package com.itesoft.registree.rest.helper;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;

public interface SpringWebHelper {
  static HttpHeaders getHeaders(final Map<String, Object> headersToSet) {
    if (headersToSet == null) {
      return null;
    }
    final HttpHeaders headers = new HttpHeaders();
    for (final Map.Entry<String, Object> entry : headersToSet.entrySet()) {
      headers.add(entry.getKey(), Objects.toString(entry.getValue()));
    }
    return headers;
  }

  static <T> URI getLocationUri(final HttpServletRequest request,
                                final T id) {
    final String baseUri = request.getRequestURI();
    return URI.create(String.format("%s/%s", baseUri, id));
  }
}
