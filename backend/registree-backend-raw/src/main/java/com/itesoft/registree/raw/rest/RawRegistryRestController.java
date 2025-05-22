package com.itesoft.registree.raw.rest;

import static com.itesoft.registree.raw.config.RawConstants.FORMAT;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.acl.AclService;
import com.itesoft.registree.acl.Permission;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.raw.config.RawRegistries;
import com.itesoft.registree.raw.rest.error.RawErrorManager;
import com.itesoft.registree.registry.api.RegistryApiRestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class RawRegistryRestController implements RegistryApiRestController {
  @Autowired
  private RawRegistries rawRegistries;

  @Autowired
  private RawErrorManager errorManager;

  @Autowired
  private AclService aclService;

  private final Map<String, RawFileManager> fileManagers = new HashMap<>();

  @Autowired
  public void registerManagers(final List<RawFileManager> fileManagers) {
    addManagers(this.fileManagers, fileManagers);
  }

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> api(final Registry registry,
                                                   final HttpServletRequest request)
    throws Exception {
    final RawOperationContext context = new RawOperationContext(fileManagers,
                                                                rawRegistries,
                                                                registry);

    final String method = request.getMethod();
    final String rawPath = request.getRequestURI();
    final String path = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);

    if (HttpMethod.GET.matches(method)) {
      return getFile(request,
                     context,
                     path);
    }

    if (HttpMethod.POST.matches(method)
      || HttpMethod.PUT.matches(method)) {
      return publishFile(request,
                         context,
                         path);
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> unauthorized(final HttpServletRequest request) {
    // TODO: return better error message
    return errorManager.getErrorResponse(HttpStatus.UNAUTHORIZED,
                                         "Unauthorized");
  }

  private ResponseEntity<StreamingResponseBody> getFile(final HttpServletRequest request,
                                                        final RawOperationContext context,
                                                        final String path)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             path),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final RawFileManager fileManager = context.getFileManager();
    return fileManager.getFile(context,
                               request,
                               path);
  }

  private ResponseEntity<StreamingResponseBody> publishFile(final HttpServletRequest request,
                                                            final RawOperationContext context,
                                                            final String path)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             path),
                                                                     Permission.WRITE);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final RawFileManager fileManager = context.getFileManager();
    return fileManager.publishFile(context,
                                   request,
                                   path);
  }

  private String getPath(final RawOperationContext context,
                         final String path) {
    final String registryName = context.getRegistry().getName();
    return String.format("/%s/%s", registryName, path);
  }

  private <T extends RawManager> void addManagers(final Map<String, T> managersMap, final List<T> managers) {
    for (final T manager : managers) {
      managersMap.put(manager.getType().getValue(), manager);
    }
  }
}
