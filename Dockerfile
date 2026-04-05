# ============================================================
# Stage 1 — Build
# Maven baixa dependências e compila o projeto.
# O cache de layers do Docker reutiliza as deps se o pom.xml
# não mudou — só recompila quando o código fonte muda.
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia só o pom.xml primeiro para cachear o download de deps
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Agora copia o código e compila (pula os testes no build da imagem)
COPY src ./src
RUN mvn package -DskipTests -q

# ============================================================
# Stage 2 — Runtime
# Imagem final mínima: só JRE, sem Maven, sem código fonte.
# Resultado: ~180MB vs ~600MB de uma imagem com JDK completo.
# ============================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Cria usuário não-root para rodar a aplicação (boa prática de segurança)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copia só o JAR gerado no stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Ajusta permissões
RUN chown appuser:appgroup app.jar

USER appuser

# Porta exposta (informativa — o docker-compose mapeia a real)
EXPOSE 8080

# JVM flags para containers:
#   -XX:+UseContainerSupport     respeita os limites de CPU/memória do container
#   -XX:MaxRAMPercentage=75.0    usa no máximo 75% da RAM disponível no container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]