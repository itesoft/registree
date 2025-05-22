package com.itesoft.registree.raw;

import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RawProxyRegistryWithExcludedFilterTest extends AbstractRawProxyRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList(EXCEPTION_TGZ_PATH),
                                   ProxyRegistryFilterPolicy.EXCLUDE,
                                   ProxyRegistryFilterPolicy.INCLUDE);
    createProxyRegistry(proxyUrl.toString(),
                        filtering);
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void getExcludedFiles() throws Exception {
    downloadFile(8,
                 "error 404",
                 PROXY_REGISTRY_NAME,
                 EXCEPTION_TGZ_PATH,
                 "exception-25.3.3-master.tgz");
  }

  @Test
  public void getNotExcludedFiles() throws Exception {
    getAndCheckFile(rpcLib,
                    PROXY_REGISTRY_NAME,
                    RPC_TGZ_PATH,
                    "rpc-25.3.3-master.tgz",
                    "application/x-tar");
  }
}
