package com.itesoft.registree.api.definition;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.DeleteComponentArgs;
import com.itesoft.registree.dto.UpdateComponentArgs;

public interface ComponentApi {
  List<Component> searchComponents(RequestContext requestContext,
                                   ResponseContext responseContext,
                                   String filter,
                                   String sort,
                                   Integer page,
                                   Integer pageSize);

  boolean componentExists(RequestContext requestContext,
                          ResponseContext responseContext,
                          @NotNull String id);

  boolean componentExists(RequestContext requestContext,
                          ResponseContext responseContext,
                          @NotNull String registryName,
                          @NotNull String gavAsString);

  Component getComponent(RequestContext requestContext,
                         ResponseContext responseContext,
                         @NotNull String id);

  Component getComponent(RequestContext requestContext,
                         ResponseContext responseContext,
                         @NotNull String registryName,
                         @NotNull String gav);

  Component createComponent(RequestContext requestContext,
                            ResponseContext responseContext,
                            @NotNull CreateComponentArgs createComponentArgs);

  Component createComponent(RequestContext requestContext,
                            ResponseContext responseContext,
                            @NotNull String registryName,
                            @NotNull CreateComponentArgs createComponentArgs);

  Component updateComponent(RequestContext requestContext,
                            ResponseContext responseContext,
                            @NotNull String id,
                            @NotNull UpdateComponentArgs updateComponentArgs);

  Component updateComponent(RequestContext requestContext,
                            ResponseContext responseContext,
                            String registryName,
                            @NotNull String gav,
                            @NotNull UpdateComponentArgs updateComponentArgs);

  void deleteComponent(RequestContext requestContext,
                       ResponseContext responseContext,
                       @NotNull String id,
                       DeleteComponentArgs deleteComponentArgs);

  void deleteComponent(RequestContext requestContext,
                       ResponseContext responseContext,
                       @NotNull String registryName,
                       @NotNull String gav,
                       DeleteComponentArgs deleteComponentArgs);
}
