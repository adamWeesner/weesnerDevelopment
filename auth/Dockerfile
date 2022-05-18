# syntax = docker/dockerfile:1.0-experimental
FROM gradle:7.1.0-jdk11 as BUILD_IMAGE

WORKDIR /app
ADD . /app
RUN gradle wrapper
RUN ./gradlew :auth:clean :auth:assemble

FROM adoptopenjdk/openjdk11:armv7l-ubuntu-jre-11.0.15_10

EXPOSE 8081:8081
EXPOSE 8444:8444

WORKDIR /app
COPY --from=0 app/auth/build/libs/auth-*-all.jar auth.jar
RUN which java
RUN java -version

CMD ["java", "-jar", "auth.jar"]