# syntax = docker/dockerfile:1.0-experimental
FROM gradle:7.1.0-jdk11 as BUILD_IMAGE

WORKDIR /app
ADD . /app
ADD ../server/*.jks /app
RUN gradle wrapper
RUN ./gradlew :backendRouter:clean :backendRouter:assemble

FROM adoptopenjdk/openjdk11:alpine-jre

EXPOSE 8080:8080
EXPOSE 8443:8443
WORKDIR /app
COPY --from=0 app/backendRouter/build/libs/backendRouter-*-all.jar backendRouter.jar
COPY --from=0 app/server /server
CMD ["java", "-jar", "backendRouter.jar"]