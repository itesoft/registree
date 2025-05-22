# Purpose

What Registree provides:
* a solution for hosting artefacts, the artefact formats currently supported are:
  * OCI (docker, Helm, etc.)
  * Maven
  * npm
  * raw (any file type)
* the ability to proxy remote registries (Docker hub, npmjs registry, Maven central, etc.)
* quick and easy deployment

# Build it

## Backend

Prerequisites:
* Java 17
* Maven 3.9+
* Docker if the docker image is wanted
* To run the tests: Docker, Helm, npm, Maven

Building Maven artefacts:
```
mvn install
```

To go faster, you can skip the tests
```
mvn install -Dmaven.test.skip=true
```

Collect all backend dependencies
```
mvn -f backend/registree-backend dependency:copy-dependencies
```

Build the docker image
```
docker build -t itesoft/registree-backend backend/
```

## Frontend

Prerequisites:
* Docker (if the docker image is wanted)
* npm (if not)

### Docker image
```
docker build -t itesoft/registree-frontend frontend/
```

### Standalone
```
> cd frontend/registree-frontend
> npm ci
> npm run build
```

# Run it

We provide a Docker file for both the backend and the frontend. But it is possible to run either without Docker.

The backend requires Java 17 and a PostgreSql database.

The frontend can be hosted on any web server.

Only the docker way of running the 2 components is presented here.

## All-in-one with Docker Compose

A [compose.yaml](example/docker-compose/compose.yaml) is provided as an example.

Once started:
* backend is exposed at http://localhost:8080
* frontend is exposed at  http://localhost:8081

## Backend

First, create a database that will store Registree data and associate it with a specific user name
```
CREATE USER registree PASSWORD 'registree';
CREATE DATABASE registree WITH OWNER=registree;
```

Then create an application.properties file and fill it in, as shown in this example:
```
# log in a file (do not forget to add a docker volume if necessary)
#logging.file.name=logs/registree-backend.log

# Connect to the database
spring.datasource.url=jdbc:postgresql://hostname:port/registree
spring.datasource.username=registree
spring.datasource.password=registree

# No timeout (since we can download/upload large files), adjust if necessary
spring.mvc.async.request-timeout=-1

# Optional LDAP authentication
################################
# The Registree part of LDAP config
# Enable use of LDAP (in addition to local user authentication)
# If false or not specified, all the other parameters are useless
ldap.auth.useLdap=true
# If necessary, filter used to find the user attempting to authenticate
ldap.auth.userFilter=(objectClass=organizationalPerson)
# If the search base differs from the LDAP base
ldap.auth.baseDn=DC=specific,DC=example,DC=com
# The LDAP user name attribute (for Microsoft AD, certainly sAMAccountName)
ldap.auth.attributes.username=uid
ldap.auth.attributes.firstName=givenName
ldap.auth.attributes.lastName=sn

# The Spring part of LDAP config
spring.ldap.urls=ldap://hostname:389
spring.ldap.base=DC=example,DC=com
spring.ldap.username=CN=user,DC=example,DC=com
spring.ldap.password=password
```

Run the backend Docker:
```
docker run -it --name registree-backend \
  -v ./registree-backend-data:/app/data \
  -v ./application.properties:/app/config/application.properties:application.properties \
  -p 8080:8080 \
  itesoft/registree-backend
```

## Frontend

Run the frontend Docker:
```
docker run -it --name registree-frontend \
  -e BACKEND_URL=http://localhost:8080/ \
  -p 8081:80 \
  itesoft/registree-frontend
```

# Configure it

For the moment there is no configuration interface in the frontend. Add/modify/delete operations can be carried out via the console provided in the frontend, or using the REST API.

## Registries

4 registry formats are currently supported:
* OCI (Docker, Helm, etc.)
* Maven
* npm
* raw

There are 3 types of registries:
* hosted: hosts artefacts
* proxy: proxies remote registries with the ability to store remote artefacts locally
* group: references other registries to serve their artefacts via a single registry

**The 'configuration' parameter** that must be passed during creation/modification is a JSON that varies depending on the format and type specifications. Details below.

### Type
#### hosted
* storagePath (string, required): a relative path to the "data" repository in which all the items of this registry will be stored (an absolute path can also be supplied).

#### proxy
* doStore (boolean, required): indicates whether the registry should create a local copy of the artefacts it downloads remotely.
* storagePath (string, required if doStore = true): a relative path to the "data" repository in which all the items of this registry will be stored (an absolute path can also be supplied).
* proxyUrl (string, required): the url of the remote proxy.
* cacheTimeout : the cache timeout in minutes (the period during which the remote proxy will not be queried), if 0: no cache.
* filtering (object, optional): an object representing the filtering(s) to be performed on this proxy.
  * defaultPolicy (enum, required): the policy to be applied by default to the filtering. In other words: when all the filters have been applied, what should be done? Possible values: INCLUDE/EXCLUDE.
  * filters (object array, required): an array of objects for each filter criterion.
    * pathPrefix (string, required): the path prefix of the artefacts to be included/excluded according to the policy.
    * policy (enum, required): the policy to apply to the pathPrefix. Possible values: INCLUDE/EXCLUDE.

#### group
* memberNames (array, required): the names of the registries that are part of this group.

### Format
#### OCI
* port (int, required): the specific nature of the OCI API requires a port to be allocated for each registry (because the OCI API considers any element in the "path" to be part of the registry name).

In the context of a proxy-type registry, additional parameters:
* proxyUsername (string, optional): the user name to be used to access the remote registry.
* proxyPassword (string, optional): the password to be used to access the remote registry.


#### Maven
-- no specificities --

#### npm
In the context of a proxy-type registry, additional parameters:
* proxyAuthToken (string, optional): the token used to access the remote registry.

#### raw
-- no specificities --


### Configuration examples

Configuration of an OCI registry x Docker hub proxy with local storage.
```
{
  "port": 7011,
  "doStore": true,
  "storagePath": "docker-proxy-dockerio",
  "proxyUrl": "https://registry-1.docker.io",
  "proxyUsername": "username",
  "proxyPassword": "password"
}
```

Configuration of a Maven registry x proxy with artefact filtering.
```
{
  "doStore": true,
  "storagePath": "maven-proxy-aspose",
  "proxyUrl": "https://artifact.aspose.com/repo",
  "filtering": {
    "filters": [
      {
        "pathPrefix": "com/aspose/",
        "policy": "INCLUDE"
      }
    ],
    "defaultPolicy": "EXCLUDE"
  }
}
```

## Users

Represents a Registree user.

By default, 2 users are created:
* "anonymous": will be used for all "unauthenticated" operations.
* "admin": (default password -> admin), user with full rights (see the [Routes section](#Routes) below).


## Routes

The idea behind routes is to associate a user with a 'path' and a 'rwd' (read/write/delete) right.

During a REST call, the route path is compared with the call path and the HTTP method is compared with the right (HEAD / GET -> r, POST / PUT -> w, DELETE -> d). If the user has the exact route, or a parent route, and the right corresponding to the method used, the call can be made. Otherwise the call fails.

Parent routes can be used, for example, to give full rights to a user by adding the '/ route' with 'rwd'. This is the case for the 'admin' user created by default.


# Todolist

Known facts to add:
* Add a github action (maybe with no tests to start with? running tests requires a number of prerequisites)
* Support 'search' API for npm
* Add validation annotation everywhere in objects used by the API (to get clear errors triggered by spring validation)
* In frontend, do not display 'console' link for every logged in user
* Add an internal user and use it as uploader of create proxy-created components (currently it's anonymous or whatever user is used on first artefact download from the client)
* Proxies:
  * Add some TTL for cached elements
  * Fast timeout when remote host is not available
  * Add real offline mode (work with local cache)
* How to deal with registry deletion when registry already contains data?
* Add detailed documentation
* Add openAPI definition for our REST API
* Add documentation on how to extend registree with other formats
* Add (a lot of) tests
* Add native build (graalvm)
* Add details on how to migrate from other tools to registree
* Add registry configuration interfaces to frontend

There are also TODOs and FIXMEs directly in the code, mainly in the backend, when something is missing or needs to be reviewed.

# Participate

Open an issue if you can't do something or if you have a question.

Feel free to submit PR if you'd like to contribute.
