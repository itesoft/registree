#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE USER registree PASSWORD 'registree';
  CREATE DATABASE registree WITH OWNER=registree;
EOSQL
