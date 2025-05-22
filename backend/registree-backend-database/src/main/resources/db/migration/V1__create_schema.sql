CREATE TABLE reg_component
(
  id VARCHAR(64) NOT NULL,
  registry_name VARCHAR(64) NOT NULL,
  group_name VARCHAR(256) NULL,
  name VARCHAR(512) NOT NULL,
  version VARCHAR(128) NOT NULL,
  creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
  update_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reg_file
(
  id VARCHAR(64) NOT NULL,
  registry_name VARCHAR(64) NOT NULL,
  component_id VARCHAR(64) NULL,
  path VARCHAR(1024) NOT NULL,
  content_type VARCHAR(128) NOT NULL,
  creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
  update_date TIMESTAMP WITH TIME ZONE NOT NULL,
  uploader VARCHAR(128) NOT NULL
);

CREATE TABLE reg_registry
(
  name VARCHAR(64) NOT NULL,
  format VARCHAR(64) NOT NULL,
  type VARCHAR(64) NOT NULL,
  configuration JSONB NULL
);

CREATE TABLE reg_route
(
  id BIGSERIAL NOT NULL,
  user_id bigint NOT NULL,
  path VARCHAR(256) NOT NULL,
  permissions VARCHAR(32) NOT NULL
);

CREATE TABLE reg_user
(
  id BIGSERIAL NOT NULL,
  username VARCHAR(128) NOT NULL,
  password VARCHAR(128) NULL,
  first_name VARCHAR(256) NULL,
  last_name VARCHAR(256) NULL
);

ALTER TABLE reg_component ADD CONSTRAINT pk_component PRIMARY KEY
(
  id
);

ALTER TABLE reg_file ADD CONSTRAINT pk_file PRIMARY KEY
(
  id
);

ALTER TABLE reg_registry ADD CONSTRAINT pk_registry PRIMARY KEY
(
  name
);

ALTER TABLE reg_route ADD CONSTRAINT pk_route PRIMARY KEY
(
  id
);

ALTER TABLE reg_user ADD CONSTRAINT pk_user PRIMARY KEY
(
  id
);

ALTER TABLE reg_route ADD CONSTRAINT fk_route__user
  FOREIGN KEY (user_id) REFERENCES reg_user (id);

ALTER TABLE reg_component ADD CONSTRAINT fk_component__registry
  FOREIGN KEY (registry_name) REFERENCES reg_registry (name);

ALTER TABLE reg_file ADD CONSTRAINT fk_file__registry
  FOREIGN KEY (registry_name) REFERENCES reg_registry (name);

ALTER TABLE reg_file ADD CONSTRAINT fk_file__component
  FOREIGN KEY (component_id) REFERENCES reg_component (id);
