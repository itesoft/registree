package com.itesoft.registree.oci.rest.error;

public abstract class ErrorCode {
  /*
   * See here:
   * https://github.com/opencontainers/distribution-spec/blob/main/spec.md#error-
   * codes code-1 BLOB_UNKNOWN blob unknown to registry code-2 BLOB_UPLOAD_INVALID
   * blob upload invalid code-3 BLOB_UPLOAD_UNKNOWN blob upload unknown to
   * registry code-4 DIGEST_INVALID provided digest did not match uploaded content
   * code-5 MANIFEST_BLOB_UNKNOWN manifest references a manifest or blob unknown
   * to registry code-6 MANIFEST_INVALID manifest invalid code-7 MANIFEST_UNKNOWN
   * manifest unknown to registry code-8 NAME_INVALID invalid repository name
   * code-9 NAME_UNKNOWN repository name not known to registry code-10
   * SIZE_INVALID provided length did not match content length code-11
   * UNAUTHORIZED authentication required code-12 DENIED requested access to the
   * resource is denied code-13 UNSUPPORTED the operation is unsupported code-14
   * TOOMANYREQUESTS too many requests
   */
  public static final String BLOB_UNKNOWN = "BLOB_UNKNOWN";
  public static final String BLOB_UPLOAD_UNKNOWN = "BLOB_UPLOAD_UNKNOWN";
  public static final String DIGEST_INVALID = "DIGEST_INVALID";
  public static final String MANIFEST_INVALID = "MANIFEST_INVALID";
  public static final String MANIFEST_UNKNOWN = "MANIFEST_UNKNOWN";
  public static final String NAME_UNKNOWN = "NAME_UNKNOWN";
  public static final String UNAUTHORIZED = "UNAUTHORIZED";
  public static final String UNSUPPORTED = "UNSUPPORTED";

  private ErrorCode() {

  }
}
