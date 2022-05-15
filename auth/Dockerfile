# syntax = docker/dockerfile:1.0-experimental
FROM adoptopenjdk/openjdk11:alpine-jre

EXPOSE 8081:8081
EXPOSE 8444:8444
WORKDIR /app
RUN curl -fsSL -o auth.jar https://$gitToken@maven.pkg.github.com/adamWeesner/weesnerDevelopment/com/weesnerdevelopment/auth/auth/$packageVersion/auth-$packageVersion-all.jar

CMD ["java", "-jar", "auth.jar"]