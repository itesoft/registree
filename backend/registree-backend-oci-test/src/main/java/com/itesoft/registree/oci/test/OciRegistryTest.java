package com.itesoft.registree.oci.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.spring.test.RegistryTest;

import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class OciRegistryTest extends RegistryTest {
  @Override
  protected String getFormat() {
    return "oci";
  }

  protected void createHostedRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("port", 8090);
    configurationAsMap.put("storagePath", "registry-hosted");

    createHostedRegistry(configurationAsMap);
  }

  protected void createGroupRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("port", 8060);
    configurationAsMap.put("memberNames", Arrays.asList("hosted", "proxy"));
    createGroupRegistry(configurationAsMap);
  }
}
