# Use a base image with Java 17, for example, the OpenJDK image
FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY incident-management.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

