package com.itesoft.registree.npm.rest.group;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.npm.dto.json.ResponsePackage;
import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.rest.NpmOperationContext;
import com.itesoft.registree.npm.rest.NpmPackageManager;
import com.itesoft.registree.npm.rest.ReadOnlyNpmPackageManager;
import com.itesoft.registree.npm.storage.PackageStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupPackageManager extends ReadOnlyNpmPackageManager implements NpmPackageManager {
  @Autowired
  private NpmGroupRegistryHelper groupRegistryHelper;

  @Autowired
  private PackageStorage packageStorage;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getPackage(final NpmOperationContext context,
                                                          final HttpServletRequest request,
                                                          final String packageScope,
                                                          final String packageName)
    throws Exception {
    final ResponsePackage responsePackage = getResponsePackage(context, request, packageScope, packageName);
    if (responsePackage == null) {
      return packageNotFound(packageScope, packageName);
    }
    final StreamingResponseBody stream = outputStream -> {
      getObjectMapper().writeValue(outputStream, responsePackage);
    };
    return ResponseEntity.ok(stream);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getPackageVersion(final NpmOperationContext context,
                                                                 final HttpServletRequest request,
                                                                 final String packageScope,
                                                                 final String packageName,
                                                                 final String packageVersion)
    throws Exception {
    final ResponsePackage responsePackage = getResponsePackage(context, request, packageScope, packageName);
    if (responsePackage == null) {
      return packageNotFound(packageScope, packageName);
    }
    final Map<String, Version> versions = responsePackage.getVersions();
    final Version version = versions.get(packageVersion);
    return getPackageVersion(context, packageScope, packageName, version);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> downloadTarball(final NpmOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String packageScope,
                                                               final String packageName,
                                                               final String fileName)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          (subContext, packageManager) -> {
                                            return packageManager.downloadTarball(subContext, request, packageScope, packageName, fileName);
                                          },
                                          String.format("Tarball '%s' for package '%s' not found",
                                                        fileName,
                                                        getFullPackageName(packageScope, packageName)));
  }

  private ResponseEntity<StreamingResponseBody> packageNotFound(final String packageScope,
                                                                final String packageName) {
    return getErrorManager().getErrorResponse(HttpStatus.NOT_FOUND,
                                              String.format("Package '%s' not found",
                                                            getFullPackageName(packageScope, packageName)));
  }

  private ResponsePackage getResponsePackage(final NpmOperationContext context,
                                             final HttpServletRequest request,
                                             final String packageScope,
                                             final String packageName)
    throws Exception {
    ResponsePackage responsePackage = null;
    final GroupRegistry groupRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupRegistry.getMemberNames()) {
      final NpmOperationContext subContext = context.createSubContext(member);
      final NpmPackageManager packageManager = subContext.getPackageManager();

      final ResponseEntity<StreamingResponseBody> response = packageManager.getPackage(subContext,
                                                                                       request,
                                                                                       packageScope,
                                                                                       packageName);
      if (HttpStatus.NOT_MODIFIED.equals(response.getStatusCode())) {
        return readResponsePackage(response);
      }
      if (!HttpStatus.OK.equals(response.getStatusCode())) {
        continue;
      }
      final ResponsePackage subResponsePackage = readResponsePackage(response);

      if (responsePackage == null) {
        responsePackage = subResponsePackage;
      } else {
        responsePackage.getVersions().putAll(subResponsePackage.getVersions());
        responsePackage.getTime().putAll(subResponsePackage.getTime());
        packageStorage.mergeDistTags(responsePackage.getDistTags(), subResponsePackage.getDistTags());
      }
    }
    return responsePackage;
  }

  private ResponsePackage readResponsePackage(final ResponseEntity<StreamingResponseBody> response)
    throws IOException {
    // TODO: optimize (write and read at same time?
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      response.getBody().writeTo(outputStream);
    } finally {
      outputStream.close();
    }
    return getObjectMapper().readValue(outputStream.toByteArray(), ResponsePackage.class);
  }
}
