# Etapa 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia dependências primeiro
COPY projeto/pom.xml .
RUN mvn dependency:go-offline -B

# Copia código fonte
COPY projeto/src ./src

# Build do projeto
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final mais leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o JAR gerado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
