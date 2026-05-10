# Estágio 1: Build
FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Estágio 2: Runtime
FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY opentelemetry-javaagent.jar otel-agent.jar

EXPOSE 8080

CMD ["java", "-javaagent:/app/otel-agent.jar", "-jar", "app.jar"]
