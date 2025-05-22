package com.itesoft.registree.maven;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.sql.init.mode=always",
                                   "spring.sql.init.data-locations=classpath:/maven-hosted-test.sql" })
public abstract class MavenHostedRegistryWithDatabaseTest extends MavenRegistryWithDatabaseTest {
  protected static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  protected void assertComponentAndFiles(final String groupId,
                                         final String artifactId,
                                         final String version,
                                         final String username)
    throws Exception {
    assertComponentAndFiles(HOSTED_REGISTRY_NAME, groupId, artifactId, version, username);
  }
}
