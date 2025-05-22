package com.itesoft.registree.registry.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.RegistryApiRestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/registry/{registryName}/**")
public class RegistryDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryDispatcher.class);

  @Autowired
  private DispatcherRegistries dispatcherRegistries;

  private final Map<String, RegistryApiRestController> registryApiRestControllers = new HashMap<>();

  @Autowired(required = false)
  public void create(final List<RegistryApiRestController> registryApiRestControllers) {
    if (registryApiRestControllers == null) {
      return;
    }

    for (final RegistryApiRestController registryApiRestController : registryApiRestControllers) {
      this.registryApiRestControllers.put(registryApiRestController.getFormat(), registryApiRestController);
    }
  }

  @RequestMapping
  public ResponseEntity<StreamingResponseBody> dispatch(final HttpServletRequest request,
                                                        @PathVariable("registryName") final String registryName) {
    final Registry registry = dispatcherRegistries.getRegistry(registryName);
    if (registry == null) {
      LOGGER.warn("Registry {} not found", registryName);
      return ResponseEntity.notFound().build();
    }

    final String format = registry.getFormat();

    final RegistryApiRestController registryApiRestController =
      registryApiRestControllers.get(format);

    if (registryApiRestController == null) {
      LOGGER.error("No controller found for format {}", format);
      return ResponseEntity.notFound().build();
    }

    final HttpServletRequest wrappedRequest = new RequestWrapper(request,
                                                                 String.format("/registry/%s",
                                                                               registryName));
    try {
      return registryApiRestController.api(registry,
                                           wrappedRequest);
    } catch (final Throwable throwable) {
      LOGGER.error(throwable.getMessage(), throwable);
      return ResponseEntity.internalServerError().build();
    }
  }
}
