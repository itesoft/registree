package com.itesoft.registree.web;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class WebPathsByPortConfiguration {
  private static class PortWebPath {
    private final int port;
    private final String path;

    PortWebPath(final int port,
                final String path) {
      this.port = port;
      this.path = path;
    }

    public int getPort() {
      return port;
    }

    public String getPath() {
      return path;
    }
  }

  private static final int DEFAULT_PORT = 8080;

  @Autowired
  private Environment environment;

  private int serverPort = DEFAULT_PORT;

  @PostConstruct
  public void setServerPort() {
    final String definedPort = environment.getProperty("server.port");
    if (definedPort != null) {
      serverPort = Integer.parseInt(definedPort);
    }
  }

  private final Queue<PortWebPath> portWebPaths = new ConcurrentLinkedQueue<>();

  public int getDefaultPort() {
    return serverPort;
  }

  public void add(final String path) {
    portWebPaths.add(new PortWebPath(serverPort, path));
  }

  public void add(final int port, final String path) {
    portWebPaths.add(new PortWebPath(port, path));
  }

  public void remove(final String path) {
    remove(serverPort, path);
  }

  public void remove(final int port, final String path) {
    for (final Iterator<PortWebPath> iterator = portWebPaths.iterator(); iterator.hasNext();) {
      final PortWebPath portWebPath = iterator.next();
      if (portWebPath.getPort() == port && path.equals(portWebPath.getPath())) {
        iterator.remove();
      }
    }
  }

  public boolean matches(final HttpServletRequest request) {
    final int webPort = request.getLocalPort();
    for (final PortWebPath portWebPath : portWebPaths) {
      if (webPort == portWebPath.getPort()) {
        final String calledPath = request.getServletPath();
        if (calledPath.startsWith(portWebPath.getPath())) {
          return true;
        }
      }
    }
    return false;
  }
}
