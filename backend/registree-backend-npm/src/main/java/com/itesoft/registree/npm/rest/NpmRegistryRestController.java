package com.itesoft.registree.npm.rest;

import static com.itesoft.registree.npm.config.NpmConstants.FORMAT;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.acl.AclService;
import com.itesoft.registree.acl.Permission;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.npm.config.NpmRegistries;
import com.itesoft.registree.npm.dto.json.CouchDBUser;
import com.itesoft.registree.npm.dto.json.TokenDto;
import com.itesoft.registree.npm.rest.error.NpmErrorManager;
import com.itesoft.registree.registry.api.RegistryApiRestController;
import com.itesoft.registree.security.auth.AuthenticationService;
import com.itesoft.registree.security.token.TokenService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class NpmRegistryRestController implements RegistryApiRestController {
  private static final String AUDITS_URL = "/-/npm/v1/security/audits";
  private static final String ADVISORIES_URL = "/-/npm/v1/security/advisories";
  private static final String LOGIN_URL = "/-/v1/login";

  private final Pattern modifyUserPattern = Pattern.compile("^/-/user/org.couchdb.user:(.+)$");
  private final Pattern simplePackagePattern = Pattern.compile("^/([^/]+)$");
  private final Pattern scopedPackagePattern = Pattern.compile("^/(@[^/]+)/([^/]+)$");
  private final Pattern simpleDownloadPackagePattern = Pattern.compile("^/([^/]+)/-/(.+)$");
  private final Pattern scopedDownloadPackagePattern = Pattern.compile("^/(@[^/]+)/([^/]+)/-/@[^/]+/(.+)$");
  private final Pattern scopedDownloadPackageAltPattern = Pattern.compile("^/(@[^/]+)/([^/]+)/-/(.+)$");
  private final Pattern getSimplePackageVersionPattern = Pattern.compile("^/([^/]+)/([^/]+)$");
  private final Pattern getScopedPackageVersionPattern = Pattern.compile("^/(@[^/]+)/([^/]+)/([^/]+)$");

  @Autowired
  private NpmRegistries npmRegistries;

  @Autowired
  private NpmErrorManager errorManager;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private AclService aclService;

  @Autowired
  private ObjectMapper objectMapper;

  private final Map<String, NpmPackageManager> packageManagers = new HashMap<>();

  @Autowired
  public void registerManagers(final List<NpmPackageManager> packageManagers) {
    addManagers(this.packageManagers, packageManagers);
  }

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override // SUPPRESS CHECKSTYLE MethodLength
  public ResponseEntity<StreamingResponseBody> api(final Registry registry,
                                                   final HttpServletRequest request)
    throws Exception {
    final NpmOperationContext context = new NpmOperationContext(packageManagers,
                                                                npmRegistries,
                                                                registry);

    final String method = request.getMethod();
    final String rawPath = request.getRequestURI();
    final String path = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);

    if (path.startsWith(AUDITS_URL)
      || path.startsWith(ADVISORIES_URL)) {
      return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    if (HttpMethod.POST.matches(method) && path.equals(LOGIN_URL)) {
      return ResponseEntity.notFound()
        .build();
    }

    if (HttpMethod.PUT.matches(method)) {
      final Matcher matcher = modifyUserPattern.matcher(path);
      if (matcher.matches()) {
        authenticationService.clearAuthentication();

        final CouchDBUser user =
          objectMapper.readValue(request.getInputStream(), CouchDBUser.class);

        final String username = user.getName();
        final String token = tokenService.createBasicToken(username, user.getPassword());
        if (token == null) {
          return errorManager.getErrorResponse(HttpStatus.UNAUTHORIZED,
                                               "Invalid credentials");
        }
        final TokenDto tokenDto = new TokenDto();
        tokenDto.setOk(String.format("You are authenticated as '%s'", username));
        tokenDto.setToken(token);
        final StreamingResponseBody stream = outputStream -> {
          objectMapper.writeValue(outputStream, tokenDto);
        };

        return ResponseEntity.created(null).body(stream);
      }
    }

    Matcher matcher = scopedPackagePattern.matcher(path);
    if (matcher.matches()) {
      final String packageScope = matcher.group(1);
      final String packageName = matcher.group(2);

      if (HttpMethod.GET.matches(method)) {
        return getPackage(request,
                          context,
                          packageScope,
                          packageName);
      }

      if (HttpMethod.PUT.matches(method)) {
        return publishPackage(request,
                              context,
                              packageScope,
                              packageName);
      }
    }

    matcher = simplePackagePattern.matcher(path);
    if (matcher.matches()) {
      final String packageName = matcher.group(1);

      if (HttpMethod.GET.matches(method)) {
        return getPackage(request,
                          context,
                          null,
                          packageName);
      }

      if (HttpMethod.PUT.matches(method)) {
        return publishPackage(request,
                              context,
                              null,
                              packageName);
      }
    }

    if (HttpMethod.GET.matches(method)) {
      matcher = scopedDownloadPackagePattern.matcher(path);
      if (matcher.matches()) {
        final String packageScope = matcher.group(1);
        final String packageName = matcher.group(2);
        final String fileName = matcher.group(3);

        return downloadPackage(request,
                               context,
                               packageScope,
                               packageName,
                               fileName);
      }

      matcher = scopedDownloadPackageAltPattern.matcher(path);
      if (matcher.matches()) {
        final String packageScope = matcher.group(1);
        final String packageName = matcher.group(2);
        final String fileName = matcher.group(3);

        return downloadPackage(request,
                               context,
                               packageScope,
                               packageName,
                               fileName);
      }

      matcher = simpleDownloadPackagePattern.matcher(path);
      if (matcher.matches()) {
        final String packageName = matcher.group(1);
        final String fileName = matcher.group(2);

        return downloadPackage(request,
                               context,
                               null,
                               packageName,
                               fileName);
      }

      matcher = getScopedPackageVersionPattern.matcher(path);
      if (matcher.matches()) {
        final String packageScope = matcher.group(1);
        final String packageName = matcher.group(2);
        final String packageVersion = matcher.group(3);

        return getPackageVersion(request,
                                 context,
                                 packageScope,
                                 packageName,
                                 packageVersion);
      }

      matcher = getSimplePackageVersionPattern.matcher(path);
      if (matcher.matches()) {
        final String packageName = matcher.group(1);
        final String packageVersion = matcher.group(2);

        return getPackageVersion(request,
                                 context,
                                 null,
                                 packageName,
                                 packageVersion);
      }
    }

    return ResponseEntity.notFound()
      .build();
  }

  private ResponseEntity<StreamingResponseBody> unauthorized(final HttpServletRequest request) {
    // TODO: return better error message
    return errorManager.getErrorResponse(HttpStatus.UNAUTHORIZED,
                                         "Unauthorized");
  }

  private ResponseEntity<StreamingResponseBody> getPackage(final HttpServletRequest request,
                                                           final NpmOperationContext context,
                                                           final String packageScope,
                                                           final String packageName)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             packageScope,
                                                                             packageName),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final NpmPackageManager packageManager = context.getPackageManager();
    return packageManager.getPackage(context,
                                     request,
                                     packageScope,
                                     packageName);
  }

  private ResponseEntity<StreamingResponseBody> getPackageVersion(final HttpServletRequest request,
                                                                  final NpmOperationContext context,
                                                                  final String packageScope,
                                                                  final String packageName,
                                                                  final String packageVersion)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             packageScope,
                                                                             packageName),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final NpmPackageManager packageManager = context.getPackageManager();
    return packageManager.getPackageVersion(context,
                                            request,
                                            packageScope,
                                            packageName,
                                            packageVersion);
  }

  private ResponseEntity<StreamingResponseBody> publishPackage(final HttpServletRequest request,
                                                               final NpmOperationContext context,
                                                               final String packageScope,
                                                               final String packageName)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             packageScope,
                                                                             packageName),
                                                                     Permission.WRITE);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final NpmPackageManager packageManager = context.getPackageManager();
    return packageManager.publishPackage(context, request, packageScope, packageName);
  }

  private ResponseEntity<StreamingResponseBody> downloadPackage(final HttpServletRequest request,
                                                                final NpmOperationContext context,
                                                                final String packageScope,
                                                                final String packageName,
                                                                final String fileName)
    throws Exception {
    final boolean isAccessAuthorized = aclService.isAccessAuthorized(getPath(context,
                                                                             packageScope,
                                                                             packageName),
                                                                     Permission.READ);
    if (!isAccessAuthorized) {
      return unauthorized(request);
    }

    final NpmPackageManager packageManager = context.getPackageManager();
    return packageManager.downloadTarball(context,
                                          request,
                                          packageScope,
                                          packageName,
                                          fileName);
  }

  private String getPath(final NpmOperationContext context,
                         final String packageScope,
                         final String packageName) {
    final String registryName = context.getRegistry().getName();
    if (packageScope == null) {
      return String.format("/%s/%s", registryName, packageName);
    }
    return String.format("/%s/%s/%s", registryName, packageScope, packageName);
  }

  private <T extends NpmManager> void addManagers(final Map<String, T> managersMap, final List<T> managers) {
    for (final T manager : managers) {
      managersMap.put(manager.getType().getValue(), manager);
    }
  }
}
