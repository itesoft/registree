package com.itesoft.registree.oci.storage;

public abstract class Constant {
  public static final String DATA_FILE_NAME = "data";
  public static final String TYPE_FILE_NAME = "type";
  public static final String LINK_FILE_NAME = "link";
  public static final String LAYERS_FOLDER_NAME = "_layers";
  public static final String MANIFEST_FOLDER_NAME = "_manifests";
  public static final String TAGS_FOLDER_NAME = "tags";
  public static final String REVISIONS_FOLDER_NAME = "revisions";
  public static final String BLOBS_PATH = "v2/blobs";
  public static final String BLOB_PATH = BLOBS_PATH + "/%s/%s/%s/%s";
  public static final String REPOSITORIES_PATH = "v2/repositories";
  private static final String REPOSITORY_PATH = REPOSITORIES_PATH + "/%s";
  public static final String UPLOAD_PATH = REPOSITORY_PATH + "/_uploads/%s";
  public static final String LAYER_PATH = REPOSITORY_PATH + "/" + LAYERS_FOLDER_NAME + "/%s/%s/" + LINK_FILE_NAME;
  public static final String MANIFESTS_PATH = REPOSITORY_PATH + "/" + MANIFEST_FOLDER_NAME;
  public static final String MANIFEST_REVISIONS_PATH = MANIFESTS_PATH + "/" + REVISIONS_FOLDER_NAME;
  public static final String MANIFEST_REVISION_PATH = MANIFEST_REVISIONS_PATH + "/%s/%s/" + LINK_FILE_NAME;
  public static final String TAGS_PATH = MANIFESTS_PATH + "/" + TAGS_FOLDER_NAME;
  public static final String MANIFEST_TAG_PATH = TAGS_PATH + "/%s";
  public static final String MANIFEST_TAG_CURRENT_PATH = MANIFEST_TAG_PATH + "/current/" + LINK_FILE_NAME;
  public static final String MANIFEST_TAG_INDEX_PATH = MANIFEST_TAG_PATH + "/index/%s/%s/" + LINK_FILE_NAME;

  public static final String BLOB_FILE_PATH = "v2/blobs/%s";
  public static final String REPOSITORY_TAG_FILE_PATH = "v2/repositories/%s/_manifests/tags/%s";

  private Constant() {
  }
}
