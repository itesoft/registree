package com.itesoft.registree.oci.rest.proxy;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.rest.OciRegistryBlobUploadManager;
import com.itesoft.registree.oci.rest.ReadOnlyRegistryBlobUploadManager;

import org.springframework.stereotype.Component;

@Component
public class ProxyRegistryBlobUploadManager extends ReadOnlyRegistryBlobUploadManager implements OciRegistryBlobUploadManager {
  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }
}
