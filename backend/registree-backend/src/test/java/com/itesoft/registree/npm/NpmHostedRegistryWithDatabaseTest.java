package com.itesoft.registree.npm;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.sql.init.mode=always",
                                   "spring.sql.init.data-locations=classpath:/npm-hosted-test.sql" })
public abstract class NpmHostedRegistryWithDatabaseTest extends NpmRegistryWithDatabaseTest {
  protected static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  protected void assertComponentAndFiles(final String scope,
                                         final String name,
                                         final String version,
                                         final String username)
    throws Exception {
    assertComponentAndFiles(HOSTED_REGISTRY_NAME, scope, name, version, username);
  }
}
