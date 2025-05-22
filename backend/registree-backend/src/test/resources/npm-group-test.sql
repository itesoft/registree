INSERT INTO reg_registry (name, format, type, configuration)
  VALUES ('hosted', 'npm', 'hosted', '{"storagePath": "registry-hosted"}');

INSERT INTO reg_registry (name, format, type, configuration)
  VALUES ('proxy', 'npm', 'proxy', '{"storagePath": "registry-proxy", "proxyUrl": "https://registry.npmjs.org"}');

INSERT INTO reg_registry (name, format, type, configuration)
  VALUES ('group', 'npm', 'group', '{"memberNames": ["hosted", "proxy"]}');
