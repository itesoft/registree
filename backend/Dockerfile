FROM eclipse-temurin:17-alpine@sha256:ca5c0c9763d21cf1617cb68ef8c17228a202cd0358fa6d7510a1bbdbd49c74da

RUN apk add --no-cache bash

WORKDIR /app

COPY dist /app

CMD ["bash", "-c", "java -jar registree-backend-*.jar"]
