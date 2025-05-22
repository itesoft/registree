package com.itesoft.registree.otel;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.UrlAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSpanAdder extends OncePerRequestFilter {
  @Autowired
  private OpenTelemetryConfiguration openTelemetryConfiguration;

  @Autowired
  private Tracer tracer;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final FilterChain filterChain)
    throws ServletException, IOException {
    final String otelTraceId = request.getHeader(openTelemetryConfiguration.getTraceIdHeaderName());

    final String path = request.getServletPath();
    final String spanName = String.format("%s %s", request.getMethod(), path);

    final SpanBuilder spanBuilder =
      tracer.spanBuilder(spanName)
        .setSpanKind(SpanKind.SERVER);
    if (otelTraceId != null) {
      final String otelSpanId = request.getHeader(openTelemetryConfiguration.getSpanIdHeaderName());
      final SpanContext remoteContext =
        SpanContext.createFromRemoteParent(otelTraceId,
                                           otelSpanId,
                                           TraceFlags.getSampled(),
                                           TraceState.getDefault());

      spanBuilder.setParent(Context.current().with(Span.wrap(remoteContext)));
    } else {
      spanBuilder.setNoParent();
    }
    final Span span = spanBuilder.startSpan();

    try (Scope scope = span.makeCurrent()) {
      filterChain.doFilter(request, response);

      span.setAttribute(HttpAttributes.HTTP_REQUEST_METHOD, request.getMethod());
      span.setAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, response.getStatus());
      span.setAttribute(HttpAttributes.HTTP_ROUTE, request.getServletPath());
      span.setAttribute(UrlAttributes.URL_PATH, request.getServletPath());

      if (response.getStatus() >= 400) {
        span.setStatus(StatusCode.ERROR);
      } else {
        span.setStatus(StatusCode.OK);
      }
    } catch (final Throwable throwable) {
      span.setStatus(StatusCode.ERROR);
      span.recordException(throwable);
      throw throwable;
    } finally {
      span.end();
    }
  }
}
