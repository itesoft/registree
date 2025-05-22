package com.itesoft.registree.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import com.itesoft.registree.api.definition.FileApi;
import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dao.SearchHelper;
import com.itesoft.registree.dao.jpa.ComponentEntity;
import com.itesoft.registree.dao.jpa.ComponentRepository;
import com.itesoft.registree.dao.jpa.FileEntity;
import com.itesoft.registree.dao.jpa.FileRepository;
import com.itesoft.registree.dao.jpa.RegistryEntity;
import com.itesoft.registree.dao.jpa.RegistryRepository;
import com.itesoft.registree.dto.CreateFileArgs;
import com.itesoft.registree.dto.DeleteFileArgs;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.dto.UpdateFileArgs;
import com.itesoft.registree.dto.mapper.FileEntityToFileMapper;
import com.itesoft.registree.exception.ConflictException;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.exception.UnprocessableException;
import com.itesoft.registree.registry.api.listener.FileOperationListener;
import com.itesoft.registree.security.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;

@Transactional(rollbackOn = Throwable.class)
@Controller
public class FileController implements FileApi {
  @Lazy
  @Autowired(required = false)
  private List<FileOperationListener> listeners;

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

  @Autowired
  private SecurityService securityService;

  @Override
  public List<File> searchFiles(final RequestContext requestContext,
                                final ResponseContext responseContext,
                                final String filter,
                                final String sort,
                                final Integer page,
                                final Integer pageSize) {
    return searchHelper.findAll(responseContext.getExtraProperties(),
                                fileRepository,
                                filter,
                                sort,
                                page,
                                pageSize,
                                FileEntityToFileMapper.PROPERTY_MAPPINGS,
                                File.class);
  }

  @Override
  public boolean fileExists(final RequestContext requestContext,
                            final ResponseContext responseContext,
                            @NotNull final String id) {
    fileRepository.lock(id);
    return fileRepository.existsById(id);
  }

  @Override
  public boolean fileExists(final RequestContext requestContext,
                            final ResponseContext responseContext,
                            @NotNull final String registryName,
                            @NotNull final String path) {
    fileRepository.lock(registryName, path);
    return fileRepository.existsByRegistryNameAndPath(registryName, path);
  }

  @Override
  public File getFile(final RequestContext requestContext,
                      final ResponseContext responseContext,
                      @NotNull final String id) {
    return doGetFile(id);
  }

  @Override
  public File getFile(final RequestContext requestContext,
                      final ResponseContext responseContext,
                      @NotNull final String registryName,
                      @NotNull final String path) {
    return doGetFile(registryName, path);
  }

  @Override
  public File createFile(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         @NotNull final CreateFileArgs createFileArgs) {
    final String registryName = createFileArgs.getRegistryName();
    final String componentId = createFileArgs.getComponentId();
    if (registryName == null && componentId == null) {
      throw new UnprocessableException("Must at least specify registryName or componentId for file creation");
    }

    RegistryEntity registryEntity = null;
    if (registryName != null) {
      registryEntity = getRegistry(registryName);
    }

    ComponentEntity componentEntity = null;
    if (componentId != null) {
      componentEntity = getComponent(componentId);
      if (registryEntity != null) {
        if (!componentEntity.getRegistry().getName().equals(registryEntity.getName())) {
          throw new UnprocessableException(String.format("File cannot be defined on registry with name [%s] and component [%s] that belongs to registry [%s]",
                                                         registryName,
                                                         componentId,
                                                         componentEntity.getRegistry().getName()));
        }
      } else {
        registryEntity = componentEntity.getRegistry();
      }
    }

    final String path = createFileArgs.getPath();
    final boolean exists =
      fileRepository.existsByRegistryNameAndPath(registryEntity.getName(),
                                                 path);
    if (exists) {
      throw new ConflictException(String.format("File with path [%s] already exists for registry with name [%s]", path, registryEntity.getName()));
    }

    final FileEntity fileEntity =
      conversionService.convert(createFileArgs, FileEntity.class);
    fileEntity.setId(UUID.randomUUID().toString());
    fileEntity.setRegistry(registryEntity);
    fileEntity.setComponent(componentEntity);
    fileEntity.setCreationDate(OffsetDateTime.now());
    fileEntity.setUpdateDate(OffsetDateTime.now());
    fileEntity.setUploader(securityService.getUsername());

    final FileEntity resultEntity = fileRepository.save(fileEntity);
    final File result = conversionService.convert(resultEntity, File.class);

    fireListeners((listener) -> listener.fileCreated(result));

    return result;
  }

  @Override
  public File updateFile(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         @NotNull final String id,
                         @NotNull final UpdateFileArgs updateFileArgs) {
    final FileEntity originalFileEntity = doGetFileEntity(id, true);
    return doUpdateFile(originalFileEntity, updateFileArgs);
  }

  @Override
  public File updateFile(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         @NotNull final String registryName,
                         @NotNull final String path,
                         @NotNull final UpdateFileArgs updateFileArgs) {
    final FileEntity originalFileEntity = doGetFileEntity(registryName, path, true);
    return doUpdateFile(originalFileEntity, updateFileArgs);
  }

  @Override
  public void deleteFile(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         @NotNull final String id,
                         final DeleteFileArgs deleteFileArgs) {
    final File file = doGetFile(id);

    fireListeners((listener) -> listener.fileDeleting(file));

    fileRepository.deleteById(file.getId());
  }

  private FileEntity doGetFileEntity(final String id,
                                     final boolean lock) {
    if (lock) {
      fileRepository.lock(id);
    }
    return fileRepository.findById(id)
      .orElseThrow(() -> new NotFoundException(String.format("File with id [%s] was not found.", id)));
  }

  private FileEntity doGetFileEntity(final String registryName, final String path, final boolean lock) {
    if (lock) {
      fileRepository.lock(registryName, path);
    }
    return fileRepository.findByRegistryNameAndPath(registryName, path)
      .orElseThrow(() -> new NotFoundException(String.format("File with path [%s] was not found on registry [%s].", path, registryName)));
  }

  private File doGetFile(final String id) {
    final FileEntity fileEntity = doGetFileEntity(id, false);
    return conversionService.convert(fileEntity, File.class);
  }

  private File doGetFile(final String registryName, final String path) {
    final FileEntity fileEntity = doGetFileEntity(registryName, path, false);
    return conversionService.convert(fileEntity, File.class);
  }

  private File doUpdateFile(final FileEntity originalFileEntity,
                            final UpdateFileArgs updateFileArgs) {
    final RegistryEntity registryEntity = originalFileEntity.getRegistry();

    final String path = updateFileArgs.getPath();
    final boolean exists =
      fileRepository.existsByRegistryNameAndPathAndIdNot(registryEntity.getName(),
                                                         path,
                                                         originalFileEntity.getId());
    if (exists) {
      throw new ConflictException(String.format("File with path [%s] already exists for registry with name [%s]", path, registryEntity.getName()));
    }

    final FileEntity fileEntity =
      conversionService.convert(updateFileArgs, FileEntity.class);

    fileEntity.setId(originalFileEntity.getId());
    fileEntity.setRegistry(registryEntity);
    fileEntity.setComponent(originalFileEntity.getComponent());
    fileEntity.setCreationDate(originalFileEntity.getCreationDate());
    fileEntity.setUpdateDate(OffsetDateTime.now());
    fileEntity.setUploader(securityService.getUsername());

    final FileEntity resultEntity = fileRepository.save(fileEntity);
    final File result = conversionService.convert(resultEntity, File.class);

    if (listeners != null) {
      final File originalFile = conversionService.convert(originalFileEntity, File.class);
      fireListeners((listener) -> listener.fileUpdated(originalFile, result));
    }

    return result;
  }

  private RegistryEntity getRegistry(final String name) {
    return registryRepository.findById(name).orElseThrow(() -> new NotFoundException(String.format("Registry with name [%s] was not found.", name)));
  }

  private ComponentEntity getComponent(final String id) {
    return componentRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Component with id [%s] was not found.", id)));
  }

  private void fireListeners(final Consumer<FileOperationListener> action) {
    if (listeners == null) {
      return;
    }
    for (final FileOperationListener listener : listeners) {
      action.accept(listener);
    }
  }
}
