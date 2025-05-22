package com.itesoft.registree.raw;

import com.itesoft.registree.raw.test.RawRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RawGroupRegistryTest extends RawRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createGroupRegistry();

    createAnonymousHostedReadWriteRoute();
    createAnonymousGroupReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted" };
  }

  @Test
  public void publishToHostedThenGetFromGroup() throws Exception {
    publishToHostedThenGetFromGroup("path/to/exception", "exception.tgz");
    publishToHostedThenGetFromGroup("path/to/rpc", "rpc.tgz");
  }

  private void publishToHostedThenGetFromGroup(final String path,
                                               final String fileName)
    throws Exception {
    publishFile(exceptionLib.toAbsolutePath().toString(),
                HOSTED_REGISTRY_NAME,
                path,
                fileName);
    getAndCheckFile(exceptionLib,
                    GROUP_REGISTRY_NAME,
                    path,
                    fileName,
                    null);
  }
}
