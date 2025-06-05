package com.itesoft.registree.oci.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("registree.oci")
public class RegistreeOciConfiguration {
  private String garbageCollectingCron = "0 0 3 * * *";

  public String getGarbageCollectingCron() {
    return garbageCollectingCron;
  }

  public void setGarbageCollectingCron(final String garbageCollectingCron) {
    this.garbageCollectingCron = garbageCollectingCron;
  }
}
