package com.itesoft.registree.oci.rest.hosted;

import static com.itesoft.registree.oci.rest.error.ErrorCode.BLOB_UNKNOWN;
import static com.itesoft.registree.oci.rest.error.ErrorCode.NAME_UNKNOWN;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.dto.Blob;
import com.itesoft.registree.oci.rest.AbstractRegistryBlobManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.OciRegistryBlobManager;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.oci.storage.BlobStorage;
import com.itesoft.registree.oci.storage.RepositoryStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedRegistryBlobManager extends AbstractRegistryBlobManager implements OciRegistryBlobManager {
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
  public ResponseEntity<StreamingResponseBody> blobExists(final OciOperationContext context,
                                                          final HttpServletRequest request,
                                                          final String name,
                                                          final String digest)
    throws IOException {
    return getBlob(context, request, name, digest, false);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String digest)
    throws Exception {
    final Blob blob = blobStorage.getBlob(context.getRegistry(),
                                          digest);
    if (blob == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           BLOB_UNKNOWN,
                                           String.format("Cannot find blob %s", digest));
    }
    return getBlobResponse(blob, true);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String name,
                                                       final String digest)
    throws IOException {
    return getBlob(context, request, name, digest, true);
  }

  private ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                        final HttpServletRequest request,
                                                        final String name,
                                                        final String digest,
                                                        final boolean withData)
    throws IOException {
    final Blob blob = getBlob(context, name, digest);
    if (blob == null) {
      final boolean repositoryExists = repositoryStorage.repositoryExists(context.getRegistry(), name);
      final String errorCode;
      final String errorMessage;
      if (repositoryExists) {
        errorCode = BLOB_UNKNOWN;
        errorMessage = String.format("Cannot find blob %s on repository %s", digest, name);
      } else {
        errorCode = NAME_UNKNOWN;
        errorMessage = String.format("Cannot find repository %s", name);
      }
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           errorCode,
                                           errorMessage);
    }

    return getBlobResponse(blob, withData);
  }
}
