# Estágio 1: Build
FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Baixa o agente OpenTelemetry durante o build, eliminando a necessidade de manter o binário no repositório.
# A versão pode ser sobrescrita via --build-arg OTEL_AGENT_VERSION=x.x.x
ARG OTEL_AGENT_VERSION=2.26.0
RUN curl -fsSL -o otel-agent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar

# Estágio 2: Runtime
FROM amazoncorretto:21-alpine

# Cria usuário e grupo sem privilégios para evitar que a aplicação rode como root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/otel-agent.jar otel-agent.jar

# Garante que o usuário da aplicação é dono dos arquivos antes de trocar de usuário
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Verifica a saúde da aplicação via Spring Actuator. start-period de 60s aguarda a JVM inicializar antes de contar falhas
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

CMD ["java", "-javaagent:/app/otel-agent.jar", "-jar", "app.jar"]
