CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE reg_user SET password=crypt(password, gen_salt('md5'));
