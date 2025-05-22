package com.itesoft.registree.raw;

import com.itesoft.registree.raw.test.RawRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RawHostedRegistryTest extends RawRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createAnonymousHostedReadWriteRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishThenGetFile() throws Exception {
    publishThenGetAndCheckFile(exceptionLib, HOSTED_REGISTRY_NAME, "path/to/exception", "exception.tgz");
    publishThenGetAndCheckFile(rpcLib, HOSTED_REGISTRY_NAME, "path/to/rpc", "rpc.tgz");
    publishThenGetAndCheckFile(rpcLib, HOSTED_REGISTRY_NAME, "", "rpc.tgz");
  }

  @Test
  public void getNotExistingFile() throws Exception {
    downloadFile(8,
                 "error 404",
                 HOSTED_REGISTRY_NAME,
                 "unknown",
                 "file.zip");
  }
}
