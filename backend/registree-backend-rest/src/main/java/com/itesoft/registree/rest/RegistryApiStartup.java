package com.itesoft.registree.rest;

import jakarta.annotation.PostConstruct;

import com.itesoft.registree.web.WebPathsByPortConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("webPathsByPortConfiguration")
public class RegistryApiStartup {
  @Autowired
  private WebPathsByPortConfiguration webPathsByPortConfiguration;

  @PostConstruct
  public void init() {
    webPathsByPortConfiguration.add("/api/v1");
  }
}
