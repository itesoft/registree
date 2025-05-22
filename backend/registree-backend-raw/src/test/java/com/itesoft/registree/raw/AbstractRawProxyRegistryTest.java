package com.itesoft.registree.raw;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itesoft.registree.raw.test.RawRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.utility.MountableFile;

public abstract class AbstractRawProxyRegistryTest extends RawRegistryTest {
  protected static final String EXCEPTION_TGZ_PATH = "path/to/exception";
  protected static final String RPC_TGZ_PATH = "rpc";
  private static final String NGINX_FOLDER = "nginx-www";

  private final NginxContainer<?> nginx = new NginxContainer<>("nginx:alpine")
    .withCopyFileToContainer(MountableFile.forHostPath(NGINX_FOLDER), "/usr/share/nginx/html")
    .waitingFor(new HttpWaitStrategy());

  protected URL proxyUrl;

  @BeforeAll
  public void setupNginx() throws Exception {
    final File nginxFolder = new File(NGINX_FOLDER);
    nginxFolder.mkdirs();
    nginxFolder.deleteOnExit();

    createFileOnNginx(EXCEPTION_TGZ_PATH, exceptionLib);
    createFileOnNginx(RPC_TGZ_PATH, rpcLib);

    nginx.start();

    proxyUrl = nginx.getBaseUrl("http", 80);
  }

  private void createFileOnNginx(final String parentFolderPath,
                                 final Path file)
    throws IOException {
    final File parentFolder = Paths.get(NGINX_FOLDER, parentFolderPath).toFile();
    parentFolder.mkdirs();
    Files.copy(file.toFile(), new File(parentFolder, file.getFileName().toString()));
  }
}
