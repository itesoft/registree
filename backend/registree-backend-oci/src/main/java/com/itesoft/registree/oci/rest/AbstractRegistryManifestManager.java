package com.itesoft.registree.oci.rest;

import java.io.IOException;

import com.itesoft.registree.oci.dto.Manifest;
import com.itesoft.registree.oci.storage.RepositoryStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class AbstractRegistryManifestManager {
  @Autowired
  private RepositoryStorage repositoryStorage;

  public HttpHeaders getGetManifestHeaders(final String contentType,
                                           final String contentLength,
                                           final String digest) {
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, contentType);
    headers.add(HttpHeaders.CONTENT_LENGTH, contentLength);
    headers.add("Docker-Content-Digest", digest);
    return headers;
  }

  public Manifest getManifest(final OciOperationContext context,
                              final String name,
                              final String reference,
                              final boolean withData)
    throws IOException {
    final String tag;
    final String digest;
    if (reference.contains("@")) {
      final String[] tab = reference.split("@");
      tag = tab[0];
      digest = tab[1];
    } else if (reference.startsWith("sha256")) {
      tag = null;
      digest = reference;
    } else {
      tag = reference;
      digest = null;
    }

    return repositoryStorage.getManifest(context.getRegistry(), name, tag, digest, withData);
  }

  public ResponseEntity<StreamingResponseBody> getManifestResponse(final Manifest manifest,
                                                                   final boolean withData) {
    final HttpHeaders headers = getGetManifestHeaders(manifest.getContentType(),
                                                      Long.toString(manifest.getContentLength()),
                                                      manifest.getDigest());

    StreamingResponseBody result = null;
    if (withData) {
      final StreamingResponseBody stream = outputStream -> {
        outputStream.write(manifest.getData());
      };
      result = stream;
    }

    return ResponseEntity.ok().headers(headers).body(result);
  }
}
