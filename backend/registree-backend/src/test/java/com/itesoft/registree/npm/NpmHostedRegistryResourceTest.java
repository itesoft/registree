package com.itesoft.registree.npm;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.itesoft.registree.dto.Resource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmHostedRegistryResourceTest extends NpmHostedRegistryWithDatabaseTest {
  private String npmrcPath;

  @BeforeAll
  public void createRoute() throws Exception {
    createAnonymousHostedReadWriteRoute();

    npmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
  }

  @Test
  public void listResources() throws Exception {
    npmPublish(npmrcPath, rpc250303Library);
    npmPublish(npmrcPath, rpc250304Library);

    List<Resource> resources =
      registryResourceClient.getRootResources(HOSTED_REGISTRY_NAME);
    assertEquals(1, resources.size());
    Resource resource = resources.get(0);
    assertEquals("@itesoft", resource.getName());
    assertEquals("@itesoft", resource.getPath());
    assertEquals(null, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "@itesoft");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("rpc", resource.getName());
    assertEquals("@itesoft/rpc", resource.getPath());
    assertEquals("@itesoft", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertEquals("@itesoft/rpc", resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "@itesoft/rpc");
    assertEquals(2, resources.size());
    resource = resources.get(0);
    assertEquals("rpc-25.3.3-master.tgz", resource.getName());
    assertEquals("@itesoft/rpc/rpc-25.3.3-master.tgz", resource.getPath());
    assertEquals("@itesoft/rpc", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("@itesoft/rpc/-/rpc-25.3.3-master.tgz", resource.getRelativeDownloadPath());
    assertEquals("@itesoft:rpc:25.3.3-master", resource.getComponentGav());
    assertEquals("@itesoft/rpc/rpc-25.3.3-master.tgz", resource.getFilePath());

    resource = resources.get(1);
    assertEquals("rpc-25.3.4-master.tgz", resource.getName());
    assertEquals("@itesoft/rpc/rpc-25.3.4-master.tgz", resource.getPath());
    assertEquals("@itesoft/rpc", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("@itesoft/rpc/-/rpc-25.3.4-master.tgz", resource.getRelativeDownloadPath());
    assertEquals("@itesoft:rpc:25.3.4-master", resource.getComponentGav());
    assertEquals("@itesoft/rpc/rpc-25.3.4-master.tgz", resource.getFilePath());
  }
}
