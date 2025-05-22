package com.itesoft.registree.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("registree.data")
public class RegistreeDataConfiguration {
  private String basePath = "data";
  private String registriesPath = basePath + "/registries";

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(final String basePath) {
    this.basePath = basePath;
  }

  public String getRegistriesPath() {
    return registriesPath;
  }

  public void setRegistriesPath(final String registriesPath) {
    this.registriesPath = registriesPath;
  }
}
