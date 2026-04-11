FROM maven:3.9.9-eclipse-temurin-21 AS builder

ARG UID=1010
ARG GID=1010

RUN groupadd -g "${GID}" group3 \
    && useradd --create-home --no-log-init -u "${UID}" -g group3 group3

WORKDIR /opt/app

COPY --chown=group3:group3 pom.xml .
RUN mvn dependency:go-offline -q

COPY --chown=group3:group3 src ./src

RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre AS runtime

ARG BUILD_DATE=unknown
ARG BUILD_VERSION=unknown
ARG IMAGE_DESCRIPTION=unknown
ARG IMAGE_NAME="Sports Pulse - MS Standings"
ARG UID=1010
ARG GID=1010

LABEL group3.sports-pulse.build-date=$BUILD_DATE \
      group3.sports-pulse.name=$IMAGE_NAME \
      group3.sports-pulse.description=$IMAGE_DESCRIPTION \
      group3.sports-pulse.base.image="eclipse-temurin:21-jre" \
      group3.sports-pulse.version=$BUILD_VERSION \
      maintainer="group3"

RUN groupadd -g "${GID}" group3 \
    && useradd --create-home --no-log-init -u "${UID}" -g group3 group3

WORKDIR /opt/app

COPY --from=builder --chown=group3:group3 /opt/app/target/ms-standings.jar app.jar

USER group3

ENTRYPOINT ["java","-jar","app.jar"]