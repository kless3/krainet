FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/notification-service-*.jar /app/notification-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "notification-service.jar"]