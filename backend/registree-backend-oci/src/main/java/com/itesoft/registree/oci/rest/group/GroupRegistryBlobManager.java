package com.itesoft.registree.oci.rest.group;

import static com.itesoft.registree.oci.rest.error.ErrorCode.BLOB_UNKNOWN;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.rest.OciRegistryBlobManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.ReadOnlyRegistryBlobManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupRegistryBlobManager extends ReadOnlyRegistryBlobManager implements OciRegistryBlobManager {
  @Autowired
  private GroupRegistryHelper groupRegistryHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> blobExists(final OciOperationContext context,
                                                          final HttpServletRequest request,
                                                          final String name,
                                                          final String digest)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          OciRegistryBlobManager.class,
                                          (subContext, registryBlobManager) -> {
                                            return registryBlobManager.blobExists(subContext, request, name, digest);
                                          },
                                          BLOB_UNKNOWN,
                                          String.format("Cannot find blob %s on repository %s", digest, name));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String digest)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          OciRegistryBlobManager.class,
                                          (subContext, registryBlobManager) -> {
                                            return registryBlobManager.getBlob(subContext, request, digest);
                                          },
                                          BLOB_UNKNOWN,
                                          String.format("Cannot find blob %s", digest));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String name,
                                                       final String digest)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          OciRegistryBlobManager.class,
                                          (subContext, registryBlobManager) -> {
                                            return registryBlobManager.getBlob(subContext, request, name, digest);
                                          },
                                          BLOB_UNKNOWN,
                                          String.format("Cannot find blob %s on repository %s", digest, name));
  }
}
