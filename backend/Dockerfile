FROM eclipse-temurin:17-alpine@sha256:9d8e2d4ff09a8b2beaa423b51814a3779469a5322208bfe6b6c4e86414576153

RUN apk add --no-cache bash

WORKDIR /app

COPY dist /app

CMD ["bash", "-c", "java -jar registree-backend-*.jar"]
