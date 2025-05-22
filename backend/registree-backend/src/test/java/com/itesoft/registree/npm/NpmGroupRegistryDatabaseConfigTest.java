package com.itesoft.registree.npm;

import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.sql.init.mode=always",
                                   "spring.sql.init.data-locations=classpath:/npm-group-test.sql",
                                   "spring.jpa.defer-datasource-initialization=false" })
public class NpmGroupRegistryDatabaseConfigTest extends NpmRegistryTest {
  private String hostedNpmrcPath;
  private String groupNpmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    createAnonymousHostedReadWriteRoute();
    createAnonymousGroupReadRoute();

    hostedNpmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
    groupNpmrcPath = createAnonymousNpmrc(GROUP_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-group" };
  }

  @Test
  public void installFromGroup() throws Exception {
    npmPublish(hostedNpmrcPath, exception250303Library);
    npmPublish(hostedNpmrcPath, rpc250304Library);
    cleanNpmStuff();

    npmView(groupNpmrcPath, "debug");
    npmInstall(groupNpmrcPath, "debug");

    cleanNpmStuff();
    npmView(groupNpmrcPath, "@itesoft/exception");
    npmInstall(groupNpmrcPath, "@itesoft/exception");
  }
}
