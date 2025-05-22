INSERT INTO reg_registry (name, format, type, configuration)
  VALUES ('hosted', 'oci', 'hosted', '{"port": 8090, "storagePath": "registry-hosted"}');

INSERT INTO reg_registry (name, format, type, configuration)
  VALUES ('proxy', 'oci', 'proxy', '{"port": 8070, "storagePath": "registry-proxy", "proxyUrl": "https://registry-1.docker.io"}');

INSERT INTO reg_registry (name, format, type, configuration)
  VALUES ('group', 'oci', 'group', '{"port": 8060, "memberNames": ["hosted", "proxy"]}');
