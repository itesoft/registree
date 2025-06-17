package com.itesoft.registree.proxy;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.apachehttpclient.v5_2.ApacheHttpClientTelemetry;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApacheHttpConfiguration {
  @Autowired(required = false)
  private OpenTelemetry openTelemetry;

  @Bean
  public CloseableHttpClient httpClient() {
    if (openTelemetry != null) {
      return ApacheHttpClientTelemetry.builder(openTelemetry)
          .build()
          .newHttpClientBuilder()
          .setRedirectStrategy(new DefaultRedirectStrategy())
          .build();
    } else {
      return HttpClientBuilder.create().setRedirectStrategy(new DefaultRedirectStrategy()).build();
    }
  }
}
