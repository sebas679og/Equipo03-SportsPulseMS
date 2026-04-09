FROM maven:3.9.9-eclipse-temurin-21 AS builder

ARG UID=1010
ARG GID=1010

RUN groupadd -g "${GID}" group6 \
    && useradd --create-home --no-log-init -u "${UID}" -g group6 group6

WORKDIR /opt/app

COPY --chown=group6:group6 pom.xml .
RUN mvn dependency:go-offline -q

COPY --chown=group6:group6 src ./src

RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre AS runtime

ARG BUILD_DATE=unknown
ARG BUILD_VERSION=unknown
ARG IMAGE_DESCRIPTION=unknown
ARG IMAGE_NAME="Sports Pulse - MS Leagues"
ARG UID=1010
ARG GID=1010

LABEL group6.sports-pulse.build-date=$BUILD_DATE \
      group6.sports-pulse.name=$IMAGE_NAME \
      group6.sports-pulse.description=$IMAGE_DESCRIPTION \
      group6.sports-pulse.base.image="eclipse-temurin:21-jre" \
      group6.sports-pulse.version=$BUILD_VERSION \
      maintainer="GROUP6"

RUN groupadd -g "${GID}" group6 \
    && useradd --create-home --no-log-init -u "${UID}" -g group6 group6

WORKDIR /opt/app

COPY --from=builder --chown=group6:group6 /opt/app/target/ms-leagues.jar app.jar

USER group6

ENTRYPOINT ["java","-jar","app.jar"]