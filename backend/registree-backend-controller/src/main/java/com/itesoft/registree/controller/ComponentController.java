package com.itesoft.registree.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import com.itesoft.registree.api.definition.ComponentApi;
import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dao.SearchHelper;
import com.itesoft.registree.dao.jpa.ComponentEntity;
import com.itesoft.registree.dao.jpa.ComponentRepository;
import com.itesoft.registree.dao.jpa.FileRepository;
import com.itesoft.registree.dao.jpa.RegistryEntity;
import com.itesoft.registree.dao.jpa.RegistryRepository;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.DeleteComponentArgs;
import com.itesoft.registree.dto.Gav;
import com.itesoft.registree.dto.UpdateComponentArgs;
import com.itesoft.registree.dto.mapper.ComponentEntityToComponentMapper;
import com.itesoft.registree.exception.ConflictException;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.registry.api.listener.ComponentOperationListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;

@Transactional(rollbackOn = Throwable.class)
@Controller
public class ComponentController implements ComponentApi {
  @Lazy
  @Autowired(required = false)
  private List<ComponentOperationListener> listeners;

  @Autowired
  private ConversionService conversionService;

  @Autowired
  private SearchHelper searchHelper;

  @Autowired
  private RegistryRepository registryRepository;

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private FileRepository fileRepository;

  @Override
  public List<Component> searchComponents(final RequestContext requestContext,
                                          final ResponseContext responseContext,
                                          final String filter,
                                          final String sort,
                                          final Integer page,
                                          final Integer pageSize) {
    return searchHelper.findAll(responseContext.getExtraProperties(),
                                componentRepository,
                                filter,
                                sort,
                                page,
                                pageSize,
                                ComponentEntityToComponentMapper.PROPERTY_MAPPINGS,
                                Component.class);
  }

  @Override
  public boolean componentExists(final RequestContext requestContext,
                                 final ResponseContext responseContext,
                                 @NotNull final String id) {
    componentRepository.lock(id);
    return componentRepository.existsById(id);
  }

  @Override
  public boolean componentExists(final RequestContext requestContext,
                                 final ResponseContext responseContext,
                                 @NotNull final String registryName,
                                 @NotNull final String gavAsString) {
    final Gav gav = conversionService.convert(gavAsString, Gav.class);
    final String group = gav.getGroup();
    if (group != null) {
      componentRepository.lock(registryName,
                               group,
                               gav.getName(),
                               gav.getVersion());
      return componentRepository.existsByRegistryNameAndGroupAndNameAndVersion(registryName,
                                                                               group,
                                                                               gav.getName(),
                                                                               gav.getVersion());
    } else {
      componentRepository.lock(registryName,
                               gav.getName(),
                               gav.getVersion());
      return componentRepository.existsByRegistryNameAndGroupIsNullAndNameAndVersion(registryName,
                                                                                     gav.getName(),
                                                                                     gav.getVersion());
    }
  }

  @Override
  public Component getComponent(final RequestContext requestContext,
                                final ResponseContext responseContext,
                                @NotNull final String id) {
    return doGetComponent(id);
  }

  @Override
  public Component getComponent(final RequestContext requestContext,
                                final ResponseContext responseContext,
                                @NotNull final String registryName,
                                @NotNull final String gav) {
    return doGetComponent(registryName, gav);
  }

  @Override
  public Component createComponent(final RequestContext requestContext,
                                   final ResponseContext responseContext,
                                   @NotNull final CreateComponentArgs createComponentArgs) {
    return doCreateComponent(createComponentArgs.getRegistryName(),
                             createComponentArgs);
  }

  @Override
  public Component createComponent(final RequestContext requestContext,
                                   final ResponseContext responseContext,
                                   @NotNull final String registryName,
                                   @NotNull final CreateComponentArgs createComponentArgs) {
    final String argRegistryName = createComponentArgs.getRegistryName();
    if (argRegistryName != null && !argRegistryName.equals(registryName)) {
      throw new ConflictException("Given registryName and args registryName differ");
    }
    return doCreateComponent(registryName,
                             createComponentArgs);
  }

  @Override
  public Component updateComponent(final RequestContext requestContext,
                                   final ResponseContext responseContext,
                                   @NotNull final String id,
                                   @NotNull final UpdateComponentArgs updateComponentArgs) {
    final ComponentEntity originalComponentEntity = doGetComponentEntity(id, true);
    return doUpdateComponent(originalComponentEntity, updateComponentArgs);
  }

  @Override
  public Component updateComponent(final RequestContext requestContext,
                                   final ResponseContext responseContext,
                                   final String registryName,
                                   @NotNull final String gav,
                                   @NotNull final UpdateComponentArgs updateComponentArgs) {
    final ComponentEntity originalComponentEntity = doGetComponentEntity(registryName, gav, true);
    return doUpdateComponent(originalComponentEntity, updateComponentArgs);
  }

  @Override
  public void deleteComponent(final RequestContext requestContext,
                              final ResponseContext responseContext,
                              @NotNull final String id,
                              final DeleteComponentArgs deleteComponentArgs) {
    final Component compoment = doGetComponent(id);
    doDeleteComponent(compoment, deleteComponentArgs);
  }

  @Override
  public void deleteComponent(final RequestContext requestContext,
                              final ResponseContext responseContext,
                              @NotNull final String registryName,
                              @NotNull final String gav,
                              final DeleteComponentArgs deleteComponentArgs) {
    final Component compoment = doGetComponent(registryName, gav);
    doDeleteComponent(compoment, deleteComponentArgs);
  }

  private ComponentEntity doGetComponentEntity(final String id, final boolean lock) {
    if (lock) {
      componentRepository.lock(id);
    }
    return componentRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Component with id [%s] was not found.",
                                                                                                  id)));

  }

  private ComponentEntity doGetComponentEntity(final String registryName,
                                               final String gavAsString,
                                               final boolean lock) {
    final ComponentEntity componentEntity;
    final Gav gav = conversionService.convert(gavAsString, Gav.class);
    if (gav.getGroup() == null) {
      if (lock) {
        componentRepository.lock(registryName,
                                 gav.getName(),
                                 gav.getVersion());
      }
      componentEntity =
        componentRepository.findByRegistryNameAndGroupIsNullAndNameAndVersion(registryName,
                                                                              gav.getName(),
                                                                              gav.getVersion())
          .orElse(null);
    } else {
      if (lock) {
        componentRepository.lock(registryName,
                                 gav.getGroup(),
                                 gav.getName(),
                                 gav.getVersion());
      }
      componentEntity =
        componentRepository.findByRegistryNameAndGroupAndNameAndVersion(registryName,
                                                                        gav.getGroup(),
                                                                        gav.getName(),
                                                                        gav.getVersion())
          .orElse(null);
    }

    if (componentEntity == null) {
      throw new NotFoundException(String.format("Component with gav [%s] was not found on registry [%s]", gavAsString, registryName));
    }
    return componentEntity;
  }

  private Component doGetComponent(final String id) {
    final ComponentEntity componentEntity = doGetComponentEntity(id, false);
    return conversionService.convert(componentEntity, Component.class);
  }

  private Component doGetComponent(final String registryName,
                                   final String gavAsString) {
    final ComponentEntity componentEntity = doGetComponentEntity(registryName, gavAsString, false);
    return conversionService.convert(componentEntity, Component.class);
  }

  private Component doCreateComponent(final String registryName,
                                      final CreateComponentArgs createComponentArgs) {
    final RegistryEntity registryEntity = getRegistry(registryName);
    final String group = createComponentArgs.getGroup();
    final boolean exists;
    if (group != null) {
      exists = componentRepository.existsByRegistryNameAndGroupAndNameAndVersion(registryEntity.getName(),
                                                                                 createComponentArgs.getGroup(),
                                                                                 createComponentArgs.getName(),
                                                                                 createComponentArgs.getVersion());
    } else {
      exists = componentRepository.existsByRegistryNameAndGroupIsNullAndNameAndVersion(registryEntity.getName(),
                                                                                       createComponentArgs.getName(),
                                                                                       createComponentArgs.getVersion());
    }
    if (exists) {
      final Gav gav = conversionService.convert(createComponentArgs, Gav.class);
      throw new ConflictException(String.format("Component with gav [%s] already exists", gav));
    }

    final ComponentEntity componentEntity =
      conversionService.convert(createComponentArgs, ComponentEntity.class);
    componentEntity.setId(UUID.randomUUID().toString());
    componentEntity.setRegistry(registryEntity);
    componentEntity.setCreationDate(OffsetDateTime.now());
    componentEntity.setUpdateDate(OffsetDateTime.now());

    final ComponentEntity resultEntity = componentRepository.save(componentEntity);
    final Component result = conversionService.convert(resultEntity, Component.class);

    fireListeners(listener -> listener.componentCreated(result));

    return result;
  }

  private Component doUpdateComponent(final ComponentEntity originalComponentEntity,
                                      final UpdateComponentArgs updateComponentArgs) {
    final String group = updateComponentArgs.getGroup();
    final boolean exists;
    if (group != null) {
      exists = componentRepository.existsByRegistryNameAndGroupAndNameAndVersionAndIdNot(originalComponentEntity.getRegistry().getName(),
                                                                                         updateComponentArgs.getGroup(),
                                                                                         updateComponentArgs.getName(),
                                                                                         updateComponentArgs.getVersion(),
                                                                                         originalComponentEntity.getId());
    } else {
      exists = componentRepository.existsByRegistryNameAndGroupIsNullAndNameAndVersionAndIdNot(originalComponentEntity.getRegistry().getName(),
                                                                                               updateComponentArgs.getName(),
                                                                                               updateComponentArgs.getVersion(),
                                                                                               originalComponentEntity.getId());
    }
    if (exists) {
      final Gav gav = conversionService.convert(updateComponentArgs, Gav.class);
      throw new ConflictException(String.format("Component with gav [%s] already exists", gav));
    }

    final ComponentEntity componentEntity =
      conversionService.convert(updateComponentArgs, ComponentEntity.class);

    componentEntity.setId(originalComponentEntity.getId());
    componentEntity.setRegistry(originalComponentEntity.getRegistry());
    componentEntity.setCreationDate(originalComponentEntity.getCreationDate());
    componentEntity.setUpdateDate(OffsetDateTime.now());

    final ComponentEntity resultEntity = componentRepository.save(componentEntity);
    final Component result = conversionService.convert(resultEntity, Component.class);

    if (listeners != null) {
      final Component originalComponent = conversionService.convert(originalComponentEntity, Component.class);
      fireListeners((listener) -> listener.componentUpdated(originalComponent, result));
    }

    return result;
  }

  private void doDeleteComponent(final Component compoment,
                                 final DeleteComponentArgs deleteComponentArgs) {
    fireListeners((listener) -> listener.componentDeleting(compoment));

    fileRepository.deleteAllByComponentId(compoment.getId());
    componentRepository.deleteById(compoment.getId());
  }

  private RegistryEntity getRegistry(final String name) {
    return registryRepository.findById(name).orElseThrow(() -> new NotFoundException(String.format("Registry with name [%s] was not found.", name)));
  }

  private void fireListeners(final Consumer<ComponentOperationListener> action) {
    if (listeners == null) {
      return;
    }
    for (final ComponentOperationListener listener : listeners) {
      action.accept(listener);
    }
  }
}
