package com.itesoft.registree.oci.rest;

import static com.itesoft.registree.oci.config.OciConstants.FORMAT;
import static com.itesoft.registree.web.WebHelper.getHost;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.acl.AclService;
import com.itesoft.registree.acl.Permission;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.oci.config.OciRegistries;
import com.itesoft.registree.oci.dto.json.TokenDto;
import com.itesoft.registree.oci.rest.error.ErrorCode;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.registry.api.RegistryApiRestController;
import com.itesoft.registree.security.auth.AuthenticationService;
import com.itesoft.registree.security.token.TokenService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class OciRegistryRestController implements RegistryApiRestController {
  static final Method API_METHOD;

  private static final Logger LOGGER = LoggerFactory.getLogger(OciRegistryRestController.class);

  static {
    try {
      API_METHOD = OciRegistryRestController.class.getMethod("api", HttpServletRequest.class);
    } catch (NoSuchMethodException | SecurityException exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  // matching these:
  // https://github.com/opencontainers/distribution-spec/blob/main/spec.md#endpoints
  // end-2 & end-10
  private final Pattern blobPattern = Pattern.compile("^/(.+)/blobs/(.+?)$");
  // end-3, end-7 & end-9
  private final Pattern manifestPattern = Pattern.compile("^/(.+)/manifests/(.+?)$");
  // end-4a, end-4b & end-11
  private final Pattern startUploadBlobPattern = Pattern.compile("^/(.+)/blobs/uploads/$");
  // end-5, end-6 & end-13
  private final Pattern uploadBlobPattern = Pattern.compile("^/(.+)/blobs/uploads/(.+?)$");
  // end-8a & end-8b
  private final Pattern listTagsPattern = Pattern.compile("^/(.+)/tags/list$");

  private final Pattern blobDirectAccessPattern = Pattern.compile("^/blobs/(.+?)$");
  private final Pattern manifestExtraPattern = Pattern.compile("^/repositories/(.+)/manifests/(.+?)$");

  @Autowired
  private OciRegistries ociRegistries;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private AclService aclService;

  @Autowired
  private ObjectMapper objectMapper;

  private final Map<String, OciRegistryBlobManager> registryBlobManagers = new HashMap<>();
  private final Map<String, OciRegistryBlobUploadManager> registryBlobUploadManagers = new HashMap<>();
  private final Map<String, OciRegistryManifestManager> registryManifestManagers = new HashMap<>();
  private final Map<String, OciRegistryRepositoryManager> registryRepositoryManagers = new HashMap<>();

  @Autowired
  public void registerManagers(final List<OciRegistryBlobManager> registryBlobManagers,
                               final List<OciRegistryBlobUploadManager> registryBlobUploadManagers,
                               final List<OciRegistryManifestManager> registryManifestManagers,
                               final List<OciRegistryRepositoryManager> registryRepositoryManagers) {
    addManagers(this.registryBlobManagers, registryBlobManagers);
    addManagers(this.registryBlobUploadManagers, registryBlobUploadManagers);
    addManagers(this.registryManifestManagers, registryManifestManagers);
    addManagers(this.registryRepositoryManagers, registryRepositoryManagers);
  }

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> api(final Registry registry,
                                                   final HttpServletRequest request)
    throws Exception {
    return process(registry, request, true);
  }

  @RequestMapping(value = "/v2/**",
                  consumes = {},
                  produces = {})
  public ResponseEntity<StreamingResponseBody> api(final HttpServletRequest request) {
    final int port = request.getLocalPort();
    final Registry registry = ociRegistries.getRegistriesPerPort().get(port);
    return process(registry, request, false);
  }

  private ResponseEntity<StreamingResponseBody> process(final Registry registry,
                                                        final HttpServletRequest request,
                                                        final boolean activateExtraPatterns) {
    if (registry == null) {
      return ResponseEntity.notFound()
        .build();
    }

    try {
      return processChecked(registry, request, activateExtraPatterns);
    } catch (final Throwable throwable) {
      LOGGER.error(throwable.getMessage(), throwable);
      return ResponseEntity.internalServerError().build();
    }
  }

  private ResponseEntity<StreamingResponseBody> processChecked(final Registry registry,
                                                               final HttpServletRequest request,
                                                               final boolean activateExtraPatterns)
    throws Exception {
    final OciOperationContext context = new OciOperationContext(registryBlobManagers,
                                                                registryBlobUploadManagers,
                                                                registryManifestManagers,
                                                                registryRepositoryManagers,
                                                                ociRegistries,
                                                                registry);

    final String method = request.getMethod();
    final String uri = request.getRequestURI();
    final String path = uri.substring(3); // remove /v2
    if (path.equals("/") && HttpMethod.GET.matches(method)) {
      return base(request, context);
    }

    if (path.equals("/token") && HttpMethod.GET.matches(method)) {
      if (!authenticationService.isAuthenticated()) {
        return unauthorized(request);
      }

      final String token = tokenService.createToken();
      final TokenDto tokenDto = new TokenDto();
      tokenDto.setToken(token);
      final StreamingResponseBody stream = outputStream -> {
        objectMapper.writeValue(outputStream, tokenDto);
      };

      return ResponseEntity.ok(stream);
    }

    if (path.equals("/_catalog") && HttpMethod.GET.matches(method)) {
      return getCatalog(request, context);
    }

    if (activateExtraPatterns && HttpMethod.GET.matches(method)) {
      Matcher matcher = manifestExtraPattern.matcher(path);
      if (matcher.matches()) {
        final String name = matcher.group(1);
        final String reference = matcher.group(2);
        return readManifest(request, context, name, reference);
      }

      matcher = blobDirectAccessPattern.matcher(path);
      if (matcher.matches()) {
        final String digest = matcher.group(1);
        return readBlob(request, context, digest);
      }
    }

    Matcher matcher = blobPattern.matcher(path);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final String digest = matcher.group(2);

      if (HttpMethod.HEAD.matches(method) || HttpMethod.GET.matches(method)) {
        return readBlob(request, context, name, digest);
      }

      if (HttpMethod.DELETE.matches(method)) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
      }
    }

    matcher = manifestPattern.matcher(path);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final String reference = matcher.group(2);

      if (HttpMethod.HEAD.matches(method) || HttpMethod.GET.matches(method)) {
        return readManifest(request, context, name, reference);
      }

      if (HttpMethod.PUT.matches(method)) {
        return writeManifest(request, context, name, reference);
      }

      if (HttpMethod.DELETE.matches(method)) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
      }
    }

    matcher = startUploadBlobPattern.matcher(path);
    if (matcher.matches() && HttpMethod.POST.matches(method)) {
      final String name = matcher.group(1);
      final String digest = request.getParameter("digest");
      final String from = request.getParameter("from");
      final String mount = request.getParameter("mount");
      return startUploadBlob(request,
                             context,
                             name,
                             digest,
                             from,
                             mount);
    }

    matcher = uploadBlobPattern.matcher(path);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final String uuid = matcher.group(2);

      if (HttpMethod.GET.matches(method)) {
        return readBlobUpload(request, context, name, uuid);
      }

      if (HttpMethod.PATCH.matches(method) || HttpMethod.PUT.matches(method)) {
        return writeBlobUpload(request, context, name, uuid);
      }
    }

    matcher = listTagsPattern.matcher(path);
    if (matcher.matches()) {
      final String name = matcher.group(1);

      if (HttpMethod.GET.matches(method)) {
        final OciRegistryRepositoryManager registryRepositoryManager = context.getRegistryRepositoryManager();
        return registryRepositoryManager.getTags(context, request, name);
      }
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> base(final HttpServletRequest request,
                                                     final OciOperationContext context) {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             null),
                                                                     Permission.WRITE);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final StreamingResponseBody stream = outputStream -> {
      outputStream.write("{}".getBytes());
    };
    return ResponseEntity.ok(stream);
  }

  private ResponseEntity<StreamingResponseBody> getCatalog(final HttpServletRequest request,
                                                           final OciOperationContext context)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context, null),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final OciRegistryRepositoryManager registryRepositoryManager = context.getRegistryRepositoryManager();
    return registryRepositoryManager.getCatalog(context, request);
  }

  private ResponseEntity<StreamingResponseBody> readBlob(final HttpServletRequest request,
                                                         final OciOperationContext context,
                                                         final String digest)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             "/"),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final OciRegistryBlobManager registryBlobManager = context.getRegistryBlobManager();
    return registryBlobManager.getBlob(context, request, digest);
  }

  private ResponseEntity<StreamingResponseBody> readBlob(final HttpServletRequest request,
                                                         final OciOperationContext context,
                                                         final String name,
                                                         final String digest)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             name),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final String method = request.getMethod();
    if (HttpMethod.HEAD.matches(method)) {
      final OciRegistryBlobManager registryBlobManager = context.getRegistryBlobManager();
      return registryBlobManager.blobExists(context, request, name, digest);
    }

    if (HttpMethod.GET.matches(method)) {
      final OciRegistryBlobManager registryBlobManager = context.getRegistryBlobManager();
      return registryBlobManager.getBlob(context, request, name, digest);
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> readManifest(final HttpServletRequest request,
                                                             final OciOperationContext context,
                                                             final String name,
                                                             final String reference)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             name),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final String method = request.getMethod();
    if (HttpMethod.HEAD.matches(method)) {
      final OciRegistryManifestManager registryManifestManager = context.getRegistryManifestManager();
      return registryManifestManager.manifestExists(context, request, name, reference);
    }

    if (HttpMethod.GET.matches(method)) {
      final OciRegistryManifestManager registryManifestManager = context.getRegistryManifestManager();
      return registryManifestManager.getManifest(context, request, name, reference);
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> writeManifest(final HttpServletRequest request,
                                                              final OciOperationContext context,
                                                              final String name,
                                                              final String reference)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             name),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final OciRegistryManifestManager registryManifestManager = context.getRegistryManifestManager();
    final byte[] manifest = StreamUtils.copyToByteArray(request.getInputStream());
    return registryManifestManager.createManifest(context, request, name, reference, manifest);
  }

  private ResponseEntity<StreamingResponseBody> startUploadBlob(final HttpServletRequest request,
                                                                final OciOperationContext context,
                                                                final String name,
                                                                final String digest,
                                                                final String from,
                                                                final String mount)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             name),
                                                                     Permission.WRITE);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final OciRegistryBlobUploadManager registryBlobUploadManager = context.getRegistryBlobUploadManager();
    if (from != null && mount != null) {
      return registryBlobUploadManager.mountBlob(context, request, name, from, mount);
    } else {
      return registryBlobUploadManager.startUpload(context, request, name, digest);
    }
  }

  private ResponseEntity<StreamingResponseBody> readBlobUpload(final HttpServletRequest request,
                                                               final OciOperationContext context,
                                                               final String name,
                                                               final String uuid)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             name),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final OciRegistryBlobUploadManager registryBlobUploadManager = context.getRegistryBlobUploadManager();
    return registryBlobUploadManager.getUploadRange(context, request, name, uuid);
  }

  private ResponseEntity<StreamingResponseBody> writeBlobUpload(final HttpServletRequest request,
                                                                final OciOperationContext context,
                                                                final String name,
                                                                final String uuid)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             name),
                                                                     Permission.WRITE);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final String method = request.getMethod();
    if (HttpMethod.PATCH.matches(method)) {
      final OciRegistryBlobUploadManager registryBlobUploadManager = context.getRegistryBlobUploadManager();
      return registryBlobUploadManager.doUploadChunk(context, request, name, uuid);
    }

    if (HttpMethod.PUT.matches(method)) {
      final OciRegistryBlobUploadManager registryBlobUploadManager = context.getRegistryBlobUploadManager();
      final String digest = request.getParameter("digest");
      return registryBlobUploadManager.doUpload(context, request, name, uuid, digest);
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> unauthorized(final HttpServletRequest request) {
    final String host = getHost(request);
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.WWW_AUTHENTICATE, String.format("Bearer realm=\"%s/v2/token\"", host));
    return errorManager.getErrorResponse(HttpStatus.UNAUTHORIZED,
                                         headers,
                                         ErrorCode.UNAUTHORIZED,
                                         "Authentication required");
  }

  private String getPath(final OciOperationContext context,
                         final String name) {
    final String registryName = context.getRegistry().getName();
    if (name == null) {
      return String.format("/%s", registryName);
    }
    return String.format("/%s/%s", registryName, name);
  }

  private <T extends OciRegistryManager> void addManagers(final Map<String, T> managersMap, final List<T> managers) {
    for (final T manager : managers) {
      managersMap.put(manager.getType().getValue(), manager);
    }
  }
}
