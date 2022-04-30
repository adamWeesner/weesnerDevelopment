# syntax = docker/dockerfile:1.0-experimental
FROM gradle:7.1.0-jdk11 as BUILD_IMAGE

WORKDIR /app
ADD . /app
ADD ../server/*.jks /app
RUN gradle wrapper
RUN ./gradlew :billMan:clean :billMan:assemble

FROM adoptopenjdk/openjdk11:alpine-jre

EXPOSE 8080:8080
EXPOSE 8443:8443
WORKDIR /app
COPY --from=0 app/billMan/build/libs/billMan-*-all.jar billMan.jar
COPY --from=0 app/server /server
CMD ["java", "-jar", "billMan.jar"]