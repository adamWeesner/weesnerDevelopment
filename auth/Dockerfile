ARG app=auth

# syntax = docker/dockerfile:1.0-experimental
FROM gradle:7.1.0-jdk11 as BUILD_IMAGE

WORKDIR /app
ADD . /app
RUN gradle wrapper
RUN ./gradlew :$app:clean :$app:assemble

FROM adoptopenjdk/openjdk11:armv7l-ubuntu-jre-11.0.15_10

WORKDIR /app
COPY --from=0 app/$app/build/libs/$app-*-all.jar $app.jar

CMD ["java", "-jar", "$app.jar"]