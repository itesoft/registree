package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import com.itesoft.registree.oci.test.OciRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HelmHostedRegistryTest extends OciRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createAnonymousHostedReadWriteRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted" };
  }

  @Test
  public void helmPushAndPull() throws Exception {
    execute("helm", "create", "test");
    execute("helm", "package", "test");
    execute("helm", "push", "test-0.1.0.tgz", "oci://localhost:8090/helm");
    execute("helm", "pull", "oci://localhost:8090/helm/test", "--plain-http", "--version", "0.1.0");
  }
}
