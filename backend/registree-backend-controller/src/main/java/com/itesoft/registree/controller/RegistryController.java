package com.itesoft.registree.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.transaction.Transactional;

import com.itesoft.registree.api.definition.RegistryApi;
import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dao.SearchHelper;
import com.itesoft.registree.dao.jpa.RegistryEntity;
import com.itesoft.registree.dao.jpa.RegistryRepository;
import com.itesoft.registree.dto.ClearProxyRegistryCacheArgs;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.DeleteRegistryArgs;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.UpdateRegistryArgs;
import com.itesoft.registree.dto.converter.RegistryEntityToRegistryConveter;
import com.itesoft.registree.exception.ConflictException;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.exception.UnprocessableException;
import com.itesoft.registree.proxy.ProxyCache;
import com.itesoft.registree.registry.RegistriesStore;
import com.itesoft.registree.registry.RegistriesValidator;
import com.itesoft.registree.registry.api.listener.RegistryListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;

// TODO: add some synchronization or lock to avoid in-mem concurrent modifications
@Transactional(rollbackOn = Throwable.class)
@Controller
public class RegistryController implements RegistryApi {
  private final Map<String, RegistryListener> listeners = new HashMap<>();

  @Autowired
  private ConversionService conversionService;

  @Autowired
  private SearchHelper searchHelper;

  @Autowired
  private RegistryRepository registryRepository;

  @Autowired
  private RegistriesStore registriesStore;

  @Autowired
  private RegistriesValidator registriesValidator;

  @Autowired
  private ProxyCache proxyCache;

  @Lazy
  @Autowired
  private void setListeners(final Collection<RegistryListener> listeners) {
    for (final RegistryListener listener : listeners) {
      this.listeners.put(listener.getFormat(), listener);
    }
  }

  @Override
  public List<Registry> searchRegistries(final RequestContext requestContext,
                                         final ResponseContext responseContext,
                                         final String filter,
                                         final String sort,
                                         final Integer page,
                                         final Integer pageSize) {
    return searchHelper.findAll(responseContext.getExtraProperties(),
                                registryRepository,
                                filter,
                                sort,
                                page,
                                pageSize,
                                RegistryEntityToRegistryConveter.PROPERTY_MAPPINGS,
                                Registry.class);
  }

  @Override
  public Registry getRegistry(final RequestContext requestContext,
                              final ResponseContext responseContext,
                              final String name) {
    return doGetRegistry(name);
  }

  @Override
  public Registry createRegistry(final RequestContext requestContext,
                                 final ResponseContext responseContext,
                                 final CreateRegistryArgs createRegistryArgs) {
    final String name = createRegistryArgs.getName();
    if (registryRepository.existsById(name)) {
      throw new ConflictException(String.format("Registry with name [%s] already exists", name));
    }

    final RegistryEntity registryEntity =
      conversionService.convert(createRegistryArgs, RegistryEntity.class);

    final RegistryEntity resultEntity = registryRepository.save(registryEntity);
    final Registry result = conversionService.convert(resultEntity, Registry.class);

    final Registry storedRegistry = fireListeners(result,
                                                  (listener) -> listener.createRegistry(result));

    registriesValidator.validateCreation(storedRegistry);

    registriesStore.store(storedRegistry);

    return result;
  }

  @Override
  public Registry updateRegistry(final RequestContext requestContext,
                                 final ResponseContext responseContext,
                                 final String name,
                                 final UpdateRegistryArgs updateRegistryArgs) {
    final Registry originalRegistry = doGetRegistry(name);

    final RegistryEntity registryEntity =
      conversionService.convert(updateRegistryArgs, RegistryEntity.class);

    registryEntity.setName(originalRegistry.getName());
    registryEntity.setFormat(originalRegistry.getFormat());
    registryEntity.setType(originalRegistry.getType());

    final RegistryEntity resultEntity = registryRepository.save(registryEntity);
    final Registry result = conversionService.convert(resultEntity, Registry.class);

    final Registry storedRegistry = fireListeners(result,
                                                  (listener) -> listener.updateRegistry(originalRegistry, result));
    registriesStore.restore(storedRegistry);

    return result;
  }

  @Override
  public void deleteRegistry(final RequestContext requestContext,
                             final ResponseContext responseContext,
                             final String name,
                             final DeleteRegistryArgs deleteRegistryArgs) {
    final Registry registry = doGetRegistry(name);

    registriesValidator.validateDeletion(registry);

    fireListeners(registry,
                  (listener) -> {
                    listener.deleteRegistry(registry);
                    return null;
                  });

    registryRepository.deleteById(name);

    registriesStore.unstore(name);
  }

  @Override
  public void clearProxyRegistryCache(final RequestContext requestContext,
                                      final ResponseContext responseContext,
                                      final String name,
                                      final ClearProxyRegistryCacheArgs clearProxyRegistryCacheArgs) {
    checkRegistryExists(name);
    proxyCache.clear(name);
  }

  private void checkRegistryExists(final String name) {
    if (!registryRepository.existsById(name)) {
      throw new NotFoundException(String.format("Registry with name [%s] was not found.", name));
    }
  }

  private Registry doGetRegistry(final String name) {
    final RegistryEntity registryEntity =
      registryRepository.findById(name).orElseThrow(() -> new NotFoundException(String.format("Registry with name [%s] was not found.", name)));
    return conversionService.convert(registryEntity, Registry.class);
  }

  private Registry fireListeners(final Registry registry,
                                 final Function<RegistryListener, Registry> action) {
    final RegistryListener listener = listeners.get(registry.getFormat());
    if (listener == null) {
      throw new UnprocessableException(String.format("Format %s is not recognized", registry.getFormat()));
    }
    return action.apply(listener);
  }
}
