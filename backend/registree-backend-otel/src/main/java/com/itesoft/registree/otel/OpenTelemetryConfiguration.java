package com.itesoft.registree.otel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("otel")
public class OpenTelemetryConfiguration {
  private static final String OTEL_SERVICE_NAME = System.getenv("OTEL_SERVICE_NAME");

  private String traceIdHeaderName = "otel-trace-id";
  private String spanIdHeaderName = "otel-span-id";

  @Bean
  public Tracer tracer(final OpenTelemetry openTelemetry) {
    return openTelemetry.getTracer(OTEL_SERVICE_NAME);
  }

  public String getTraceIdHeaderName() {
    return traceIdHeaderName;
  }

  public void setTraceIdHeaderName(final String traceIdHeaderName) {
    this.traceIdHeaderName = traceIdHeaderName;
  }

  public String getSpanIdHeaderName() {
    return spanIdHeaderName;
  }

  public void setSpanIdHeaderName(final String spanIdHeaderName) {
    this.spanIdHeaderName = spanIdHeaderName;
  }
}
