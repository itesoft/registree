FROM debian:bookworm

ARG MAVEN_VERSION=3.9.10
ARG NODE_VERSION=22

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8' \
  JAVA_HOME=/opt/java/openjdk \
  ANT_HOME=/opt/ant \
  MVN_HOME=/opt/maven \
  PATH="/opt/java/openjdk/bin:/opt/ant/bin:/opt/maven/bin:$PATH"

COPY --from=eclipse-temurin:17-jammy /opt/java/openjdk /opt/java/openjdk

RUN apt-get update && apt-get install --no-install-suggests --no-install-recommends -y ca-certificates wget curl gnupg lsb-release net-tools \
  # locales
  && apt-get -y install locales \
  && echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
  && locale-gen en_US.UTF-8 \
  # install docker
  && mkdir -p /etc/apt/keyrings \
  && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
  && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" \
       | tee /etc/apt/sources.list.d/docker.list > /dev/null \
  && apt-get update \
  && apt-get -y install docker-ce-cli \
  # install maven
  && wget --no-check-certificate http://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && tar -zvxf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt/ \
  && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
  && rm -rf /opt/maven/manual \
  && rm -f apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  # install node
  && curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - \
  && apt-get install -y nodejs \
  # install helm
  && curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash \
  # cleanup
  && apt-get clean \
  && apt-get autoremove -y

COPY entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
