package com.itesoft.registree.oci.test;

import java.io.IOException;

public final class DockerHelper {
  public static void removeDockerImages(final String imageName) throws IOException, InterruptedException {
    doRemoveDockerImages("*/" + imageName);
    doRemoveDockerImages(imageName);
  }

  private static void doRemoveDockerImages(final String arg) throws IOException, InterruptedException {
    final Process process = new ProcessBuilder()
      .command("sh", "-c", "docker images --filter reference=" + arg + " --format \"{{.ID}}\" | xargs docker rmi")
      .redirectErrorStream(true)
      .start();
    process.waitFor();
  }

  private DockerHelper() {
  }
}
