package com.itesoft.registree.oci.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.itesoft.registree.oci.dto.Blob;
import com.itesoft.registree.oci.storage.BlobStorage;
import com.itesoft.registree.oci.storage.RepositoryStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class AbstractRegistryBlobManager {
  @Autowired
  private BlobStorage blobStorage;

  @Autowired
  private RepositoryStorage repositoryStorage;

  public HttpHeaders getGetBlobHeaders(final String contentType,
                                       final String contentLength,
                                       final String digest) {
    final String actualContentType =
      contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, actualContentType);
    headers.add(HttpHeaders.CONTENT_LENGTH, contentLength);
    headers.add("Docker-Content-Digest", digest);
    return headers;
  }

  public Blob getBlob(final OciOperationContext context,
                      final String name,
                      final String digest)
    throws IOException {
    final String blobDigest = repositoryStorage.getLayerDigest(context.getRegistry(), name, digest);
    if (blobDigest == null) {
      return null;
    }
    return blobStorage.getBlob(context.getRegistry(),
                               blobDigest);
  }

  public ResponseEntity<StreamingResponseBody> getBlobResponse(final Blob blob,
                                                               final boolean withData) {
    final HttpHeaders headers = getGetBlobHeaders(blob.getContentType(),
                                                  Long.toString(blob.getContentLength()),
                                                  blob.getDigest());

    StreamingResponseBody stream = null;
    if (withData) {
      stream = outputStream -> {
        final byte[] buffer = new byte[10240];
        try (InputStream inputStream = Files.newInputStream(blob.getPath())) {
          int read;
          while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
          }
        }
      };
    }

    return ResponseEntity.ok().headers(headers).body(stream);
  }
}
