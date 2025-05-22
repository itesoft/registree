package com.itesoft.registree;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class TestConfig {
  @Bean
  public ConversionService conversionService() {
    return new DefaultConversionService();
  }
}
