# Blitz 智能商城 — Dockerfile (multi-stage build)
# Build: docker build -t blitz-server:1.0.0 .
# Run:   docker run -d --name blitz-server --network host --env-file .env blitz-server:1.0.0

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY blitz-common/pom.xml blitz-common/
COPY blitz-data/pom.xml blitz-data/
COPY blitz-security/pom.xml blitz-security/
COPY blitz-product/pom.xml blitz-product/
COPY blitz-order/pom.xml blitz-order/
COPY blitz-seckill/pom.xml blitz-seckill/
COPY blitz-ai/pom.xml blitz-ai/
COPY blitz-user/pom.xml blitz-user/
COPY blitz-server/pom.xml blitz-server/
RUN mvn dependency:go-offline -B -q || true
COPY . .
RUN mvn package -pl blitz-server -am -DskipTests -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/blitz-server/target/blitz-server-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
