FROM gradle:8.5-jdk21 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
ARG NEW_RELIC_LICENSE_KEY
ARG NEW_RELIC_APP_NAME

ENV NEW_RELIC_LICENSE_KEY=$NEW_RELIC_LICENSE_KEY
ENV NEW_RELIC_APP_NAME=$NEW_RELIC_APP_NAME

RUN gradle assemble

FROM amazoncorretto:21-alpine
EXPOSE 4322
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
COPY --from=build /home/gradle/src/newrelic/newrelic.jar /newrelic.jar
COPY --from=build /home/gradle/src/newrelic/newrelic.yml /newrelic.yml

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "-javaagent:/newrelic.jar", "-Dnewrelic.config.license_key=${NEW_RELIC_LICENSE_KEY}", "-Dnewrelic.config.app_name=${NEW_RELIC_APP_NAME}", "/app/spring-boot-application.jar"]