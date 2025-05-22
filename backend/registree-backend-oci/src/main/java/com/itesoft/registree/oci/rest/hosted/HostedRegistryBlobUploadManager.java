package com.itesoft.registree.oci.rest.hosted;

import static com.itesoft.registree.oci.rest.error.ErrorCode.BLOB_UPLOAD_UNKNOWN;
import static com.itesoft.registree.oci.rest.error.ErrorCode.DIGEST_INVALID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.dto.BlobUpload;
import com.itesoft.registree.oci.rest.AbstractRegistryBlobUploadManager;
import com.itesoft.registree.oci.rest.OciRegistryBlobUploadManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.error.ErrorCode;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.oci.storage.BlobStorage;
import com.itesoft.registree.oci.storage.RepositoryStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedRegistryBlobUploadManager extends AbstractRegistryBlobUploadManager implements OciRegistryBlobUploadManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(HostedRegistryBlobUploadManager.class);

  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private RepositoryStorage repositoryStorage;

  @Autowired
  private BlobStorage blobStorage;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getUploadRange(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String uuid)
    throws Exception {
    final BlobUpload blobUpload = blobStorage.createBlobUpload(context.getRegistry(), name);
    if (blobUpload == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           ErrorCode.BLOB_UPLOAD_UNKNOWN,
                                           String.format("Cannot find upload for %s with uuid %s", name, uuid));
    }
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.LOCATION, String.format("/v2/%s/blobs/uploads/%s", name, uuid));
    headers.add("Range", String.format("0-%d", blobUpload.getOffset()));
    return ResponseEntity.noContent()
      .headers(headers)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> mountBlob(final OciOperationContext context,
                                                         final HttpServletRequest request,
                                                         final String name,
                                                         final String from,
                                                         final String mount)
    throws Exception {
    final String blobDigest = repositoryStorage.getLayerDigest(context.getRegistry(), from, mount);
    if (blobDigest == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           ErrorCode.BLOB_UNKNOWN,
                                           String.format("Cannot find blob %s on repository %s", mount, from));
    }
    repositoryStorage.createLayer(context.getRegistry(), name, blobDigest);

    final String location = String.format("/v2/%s/blobs/%s", name, blobDigest);
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_LENGTH, "0");
    headers.add(HttpHeaders.LOCATION, location);
    headers.add("Docker-Content-Digest", blobDigest);

    return ResponseEntity.created(URI.create(location))
      .headers(headers)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> startUpload(final OciOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String name,
                                                           final String digest)
    throws IOException {
    final BlobUpload blobUpload = blobStorage.createBlobUpload(context.getRegistry(), name);
    if (digest != null) {
      return doUpload(context, request, name, blobUpload.getUuid(), digest, true);
    } else {
      return uploadAccepted(name, blobUpload.getUuid(), 0);
    }
  }

  public ResponseEntity<StreamingResponseBody> cancelUpload(final OciOperationContext context,
                                                            final String name,
                                                            final String uuid)
    throws IOException {
    final boolean exists = blobStorage.blobUploadExists(context.getRegistry(), name, uuid);
    if (!exists) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           ErrorCode.BLOB_UPLOAD_UNKNOWN,
                                           String.format("Cannot find upload for %s with uuid %s", name, uuid));
    }
    blobStorage.cancelBlobUpload(context.getRegistry(), name, uuid);
    return ResponseEntity.noContent()
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> doUpload(final OciOperationContext context,
                                                        final HttpServletRequest request,
                                                        final String name,
                                                        final String uuid,
                                                        final String digest)
    throws IOException {
    return doUpload(context, request, name, uuid, digest, true);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> doUploadChunk(final OciOperationContext context,
                                                             final HttpServletRequest request,
                                                             final String name,
                                                             final String uuid)
    throws IOException {
    return doUpload(context, request, name, uuid, null, false);
  }

  private ResponseEntity<StreamingResponseBody> doUpload(final OciOperationContext context,
                                                         final HttpServletRequest request,
                                                         final String name,
                                                         final String uuid,
                                                         final String digest,
                                                         final boolean isComplete)
    throws IOException {
    if (isComplete && digest == null) {
      LOGGER.error("[{}] Missing digest on PUT operation",
                   context.getRegistry().getName());
      return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                           DIGEST_INVALID,
                                           "Digest must be provided");
    }

    final BlobUpload blobUpload = blobStorage.getBlobUpload(context.getRegistry(), name, uuid);
    if (blobUpload == null) {
      LOGGER.error("[{}] Cannot find blob upload with uuid [{}]",
                   context.getRegistry().getName(),
                   uuid);
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           BLOB_UPLOAD_UNKNOWN,
                                           String.format("Upload uuid %s", uuid));
    }

    long bytesToRead = -1;
    final String range = request.getHeader("range");
    if (range != null) {
      final String[] tab = range.split("-");
      if (tab.length != 2) {
        LOGGER.error("[{}] Unexpected range format [{}]",
                     context.getRegistry().getName(),
                     range);
        return uploadRequestedRangeNotSatisfiable(name, uuid, blobUpload.getOffset());
      }

      long startOffset;
      long endOffset;
      try {
        startOffset = Long.parseLong(tab[0]);
        endOffset = Long.parseLong(tab[1]);
      } catch (final NumberFormatException exception) {
        LOGGER.error(String.format("[%s] Unexpected range format [%s]",
                                   context.getRegistry().getName(),
                                   range),
                     exception);
        return uploadRequestedRangeNotSatisfiable(name, uuid, blobUpload.getOffset());
      }

      if (startOffset != blobUpload.getOffset() + 1) {
        LOGGER.error("[{}] Unexpected start offset in range [{}], expecting [{}]",
                     context.getRegistry().getName(),
                     range,
                     blobUpload.getOffset() + 1);
        return uploadRequestedRangeNotSatisfiable(name, uuid, blobUpload.getOffset());
      }
      bytesToRead = startOffset - endOffset;
    }

    final long read = readInputStreamToBlobUpload(request, blobUpload);
    if (read == -1
      || (bytesToRead != -1 && bytesToRead != read)) {
      if (read != -1) { // already logged
        LOGGER.error("[{}] Reading blob bytes failed, should read [{}], read [{}]",
                     context.getRegistry().getName(),
                     bytesToRead,
                     read);
      }
      return uploadRequestedRangeNotSatisfiable(name, uuid, blobUpload.getOffset());
    }

    blobUpload.incrementLength(read);

    if (isComplete) {
      blobStorage.createBlobFromUpload(context.getRegistry(), name, digest, uuid);
      repositoryStorage.createLayer(context.getRegistry(), name, digest);

      final String location = String.format("/v2/%s/blobs/%s", name, digest);
      final HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_LENGTH, "0");
      headers.add(HttpHeaders.LOCATION, location);
      headers.add("Docker-Content-Digest", digest);

      return ResponseEntity.created(URI.create(location))
        .headers(headers)
        .build();
    } else {
      return uploadAccepted(name, uuid, blobUpload.getOffset());
    }
  }

  private ResponseEntity<StreamingResponseBody> uploadAccepted(final String name,
                                                               final String uuid,
                                                               final long offset) {
    return uploadStatus(HttpStatus.ACCEPTED,
                        name,
                        uuid,
                        offset);
  }

  private ResponseEntity<StreamingResponseBody> uploadRequestedRangeNotSatisfiable(final String name,
                                                                                   final String uuid,
                                                                                   final long offset) {
    return uploadStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE,
                        name,
                        uuid,
                        offset);
  }

  private ResponseEntity<StreamingResponseBody> uploadStatus(final HttpStatus status,
                                                             final String name,
                                                             final String uuid,
                                                             final long offset) {
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_LENGTH, "0");
    headers.add(HttpHeaders.LOCATION, String.format("/v2/%s/blobs/uploads/%s", name, uuid));
    headers.add("Range", String.format("0-%d", offset));
    headers.add("Docker-Upload-UUID", uuid);

    return ResponseEntity.status(status).headers(headers).build();
  }

  private long readInputStreamToBlobUpload(final HttpServletRequest request,
                                           final BlobUpload blobUpload)
    throws IOException {
    final OutputStream outputStream = blobUpload.getOutputStream();
    long totalBytes = 0;
    try (InputStream inputStream = request.getInputStream()) {
      if (inputStream != null) {
        final byte[] buffer = new byte[10240];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) > 0) {
          totalBytes += bytesRead;
          outputStream.write(buffer, 0, bytesRead);
          blobUpload.setLastUpdate(System.currentTimeMillis());
        }
      }
      return totalBytes;
    }
  }
}
