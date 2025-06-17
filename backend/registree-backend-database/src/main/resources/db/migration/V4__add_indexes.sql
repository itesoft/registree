CREATE INDEX IF NOT EXISTS ix_registry__name ON reg_registry
(
  name
);

CREATE INDEX IF NOT EXISTS ix_component__registry_name_group_name_name_version ON reg_component
(
  registry_name,
  group_name,
  name,
  version
);

CREATE INDEX IF NOT EXISTS ix_file__registry_name_path ON reg_file
(
  registry_name,
  path
);
