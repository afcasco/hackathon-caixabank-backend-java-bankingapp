FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src /app/src
RUN mvn clean package -DskipTests
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/bankingapp-0.0.1-SNAPSHOT.jar bankingapp.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "bankingapp.jar"]
