# Etapa 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia apenas os arquivos de dependência primeiro (cache otimizado)
COPY pom.xml .
# Baixa todas as dependências
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Build do projeto
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final mais leve
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta (ajuste conforme sua aplicação)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]