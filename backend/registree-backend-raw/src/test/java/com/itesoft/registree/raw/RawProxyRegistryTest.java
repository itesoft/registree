package com.itesoft.registree.raw;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RawProxyRegistryTest extends AbstractRawProxyRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  @BeforeAll
  public void setup() throws Exception {
    createProxyRegistry(proxyUrl.toString());
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void getFiles() throws Exception {
    getAndCheckFile(exceptionLib,
                    PROXY_REGISTRY_NAME,
                    EXCEPTION_TGZ_PATH,
                    "exception-25.3.3-master.tgz",
                    "application/octet-stream");
    getAndCheckFile(rpcLib,
                    PROXY_REGISTRY_NAME,
                    RPC_TGZ_PATH,
                    "rpc-25.3.3-master.tgz",
                    "application/x-tar");
  }
}
