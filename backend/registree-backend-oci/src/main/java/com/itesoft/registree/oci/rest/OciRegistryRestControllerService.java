package com.itesoft.registree.oci.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Service
public class OciRegistryRestControllerService {
  private static final String REGISTRY_PATH = "/v2/**";

  private final Map<Integer, Connector> connectorsByPort = new ConcurrentHashMap<>();
  private final Map<Integer, RequestMappingInfo> mappingByPort = new ConcurrentHashMap<>();

  @Autowired
  @Qualifier("requestMappingHandlerMapping")
  private RequestMappingHandlerMapping requestHandlerMapper;

  private Tomcat tomcat;

  @Autowired
  void setTomcat(final ServletWebServerApplicationContext context) {
    tomcat = ((TomcatWebServer) context.getWebServer()).getTomcat();
  }

  public void addMapping(final int port) {
    final Connector connector = new Connector(new Http11NioProtocol());
    connector.setThrowOnFailure(true);
    connector.setPort(port);
    try {
      tomcat.getService().addConnector(connector);
      connectorsByPort.put(port, connector);
    } catch (final IllegalArgumentException exception) {
      tomcat.getService().removeConnector(connector);
      throw new IllegalArgumentException(exception.getMessage(), exception);
    }
    final RequestMappingInfo mapping = RequestMappingInfo.paths(REGISTRY_PATH).build();
    requestHandlerMapper.registerMapping(mapping,
                                         "ociRegistryRestController",
                                         OciRegistryRestController.API_METHOD);
  }

  public void removeMapping(final int port) {
    final Connector connector = connectorsByPort.get(port);
    if (connector != null) {
      tomcat.getService().removeConnector(connector);
      try {
        connector.destroy();
      } catch (final LifecycleException exception) {
        throw new IllegalArgumentException(exception.getMessage(), exception);
      }
    }

    final RequestMappingInfo mapping = mappingByPort.get(port);
    if (mapping != null) {
      requestHandlerMapper.unregisterMapping(mapping);
    }
  }
}
