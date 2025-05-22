package com.itesoft.registree.npm.rest.hosted;

import static com.itesoft.registree.npm.dto.PackageExistenceState.MATCHES;
import static com.itesoft.registree.npm.dto.PackageExistenceState.UNKNOWN;

import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.npm.dto.PackageExistenceState;
import com.itesoft.registree.npm.dto.json.OkDto;
import com.itesoft.registree.npm.dto.json.RequestPackage;
import com.itesoft.registree.npm.rest.AbstractPackageManager;
import com.itesoft.registree.npm.rest.NpmOperationContext;
import com.itesoft.registree.npm.rest.NpmPackageManager;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedPackageManager extends AbstractPackageManager implements NpmPackageManager {
  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getPackage(final NpmOperationContext context,
                                                          final HttpServletRequest request,
                                                          final String packageScope,
                                                          final String packageName)
    throws Exception {
    final String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
    final PackageExistenceState existenceState =
      getPackageStorage().packageExists(context.getRegistry(),
                                        packageScope,
                                        packageName,
                                        ifNoneMatch);
    if (existenceState == UNKNOWN) {
      return getErrorManager().getErrorResponse(HttpStatus.NOT_FOUND,
                                                String.format("Package '%s' not found",
                                                              getFullPackageName(packageScope, packageName)));
    } else if (existenceState == MATCHES) {
      final HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.ETAG, ifNoneMatch);

      return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
        .headers(headers)
        .build();
    }

    return getPackageLocal(context,
                           request,
                           packageScope,
                           packageName);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getPackageVersion(final NpmOperationContext context,
                                                                 final HttpServletRequest request,
                                                                 final String packageScope,
                                                                 final String packageName,
                                                                 final String packageVersion)
    throws Exception {
    return getPackageVersionLocal(context,
                                  request,
                                  packageScope,
                                  packageName,
                                  packageVersion);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> publishPackage(final NpmOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String packageScope,
                                                              final String packageName)
    throws Exception {
    final RequestPackage requestPackage =
      getObjectMapper().readValue(request.getInputStream(), RequestPackage.class);

    final String fullPackageName = getFullPackageName(packageScope, packageName);
    if (!fullPackageName.equals(requestPackage.getName())) {
      return getErrorManager().getErrorResponse(HttpStatus.BAD_REQUEST,
                                                "Given parameters do not match called URI");
    }

    final String checksum = getPackageStorage().publishPackage(context.getRegistry(),
                                                               packageScope,
                                                               packageName,
                                                               requestPackage);
    final StreamingResponseBody stream = outputStream -> {
      final OkDto ok = OkDto.builder()
        .ok("created new package")
        .build();
      getObjectMapper().writeValue(outputStream, ok);
    };

    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ETAG, checksum);

    return ResponseEntity.created(null)
      .headers(headers)
      .body(stream);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> downloadTarball(final NpmOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String packageScope,
                                                               final String packageName,
                                                               final String fileName)
    throws Exception {
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
}
