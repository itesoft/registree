package com.itesoft.registree.oci.rest.group;

import static com.itesoft.registree.oci.rest.error.ErrorCode.MANIFEST_UNKNOWN;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.rest.OciRegistryManifestManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.ReadOnlyRegistryManifestManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupRegistryManifestManager extends ReadOnlyRegistryManifestManager implements OciRegistryManifestManager {
  @Autowired
  private GroupRegistryHelper groupRegistryHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> manifestExists(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String reference)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          OciRegistryManifestManager.class,
                                          (subContext, registryManifestManager) -> {
                                            return registryManifestManager.manifestExists(subContext, request, name, reference);
                                          },
                                          MANIFEST_UNKNOWN,
                                          String.format("Cannot find manifest with ref %s on repository %s", reference, name));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getManifest(final OciOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String name,
                                                           final String reference)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          OciRegistryManifestManager.class,
                                          (subContext, registryManifestManager) -> {
                                            return registryManifestManager.getManifest(subContext, request, name, reference);
                                          },
                                          MANIFEST_UNKNOWN,
                                          String.format("Cannot find manifest with ref %s on repository %s", reference, name));
  }
}
