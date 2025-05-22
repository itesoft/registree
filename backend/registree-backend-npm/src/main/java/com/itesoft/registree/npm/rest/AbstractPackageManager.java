package com.itesoft.registree.npm.rest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.npm.dto.GetPackageResult;
import com.itesoft.registree.npm.dto.json.ResponsePackage;
import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.rest.error.NpmErrorManager;
import com.itesoft.registree.npm.storage.PackageStorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class AbstractPackageManager implements NpmPackageManager {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PackageStorage packageStorage;

  @Autowired
  private NpmErrorManager errorManager;

  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  protected PackageStorage getPackageStorage() {
    return packageStorage;
  }

  protected NpmErrorManager getErrorManager() {
    return errorManager;
  }

  protected ResponseEntity<StreamingResponseBody> getPackageLocal(final NpmOperationContext context,
                                                                  final HttpServletRequest request,
                                                                  final String packageScope,
                                                                  final String packageName)
    throws Exception {
    final GetPackageResult getPackageResult = packageStorage.getPackage(context.getRegistry(),
                                                                        getRegistryUri(request),
                                                                        packageScope,
                                                                        packageName);
    if (getPackageResult == null) {
      return getErrorManager().getErrorResponse(HttpStatus.NOT_FOUND,
                                                String.format("Package '%s' not found",
                                                              getFullPackageName(packageScope, packageName)));
    }
    return getPackage(getPackageResult);
  }

  protected ResponseEntity<StreamingResponseBody> getPackage(final GetPackageResult getPackageResult) {
    final ResponsePackage responsePackage = getPackageResult.getResponsePackage();
    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, responsePackage);
    };

    final String checksum = getPackageResult.getChecksum();
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ETAG, checksum);

    return ResponseEntity.ok().headers(headers).body(stream);
  }

  protected ResponseEntity<StreamingResponseBody> getPackageVersionLocal(final NpmOperationContext context,
                                                                         final HttpServletRequest request,
                                                                         final String packageScope,
                                                                         final String packageName,
                                                                         final String packageVersion)
    throws Exception {
    final Version result = packageStorage.getPackageVersion(context.getRegistry(),
                                                            getRegistryUri(request),
                                                            packageScope,
                                                            packageName,
                                                            packageVersion);

    return getPackageVersion(context,
                             packageScope,
                             packageName,
                             result);
  }

  protected ResponseEntity<StreamingResponseBody> getPackageVersion(final NpmOperationContext context,
                                                                    final String packageScope,
                                                                    final String packageName,
                                                                    final Version version) {
    if (version == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           String.format("Package '%s@%s' not found",
                                                         getFullPackageName(packageScope, packageName),
                                                         version));
    }

    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, version);
    };
    return ResponseEntity.ok(stream);
  }

  protected ResponseEntity<StreamingResponseBody> downloadTarballLocal(final NpmOperationContext context,
                                                                       final String packageScope,
                                                                       final String packageName,
                                                                       final String fileName) {
    final Path tarballPath = getPackageStorage().getTarballFilePath(context.getRegistry(),
                                                                    packageScope,
                                                                    packageName,
                                                                    fileName);
    if (tarballPath == null) {
      return getErrorManager().getErrorResponse(HttpStatus.NOT_FOUND,
                                                String.format("Tarball '%s' for package '%s' not found",
                                                              fileName,
                                                              getFullPackageName(packageScope, packageName)));
    }
    return downloadTarballLocal(tarballPath);
  }

  protected ResponseEntity<StreamingResponseBody> downloadTarballLocal(final Path tarballPath) {

    final byte[] buffer = new byte[10240];
    final StreamingResponseBody stream = outputStream -> {
      try (InputStream inputStream = Files.newInputStream(tarballPath)) {
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, read);
        }
      }
    };

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(stream);
  }
}
