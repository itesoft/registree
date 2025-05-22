package com.itesoft.registree.raw;

import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RawProxyRegistryWithIncludeFilterTest extends AbstractRawProxyRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList(EXCEPTION_TGZ_PATH),
                                   ProxyRegistryFilterPolicy.INCLUDE,
                                   ProxyRegistryFilterPolicy.EXCLUDE);
    createProxyRegistry(proxyUrl.toString(),
                        filtering);
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void getIncludedFiles() throws Exception {
    getAndCheckFile(exceptionLib,
                    PROXY_REGISTRY_NAME,
                    EXCEPTION_TGZ_PATH,
                    "exception-25.3.3-master.tgz",
                    "application/octet-stream");
  }

  @Test
  public void getNotIncludedFiles() throws Exception {
    downloadFile(8,
                 "error 404",
                 PROXY_REGISTRY_NAME,
                 RPC_TGZ_PATH,
                 "rpc-25.3.3-master.tgz");
  }
}
