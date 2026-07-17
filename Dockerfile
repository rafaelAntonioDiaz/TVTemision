# ============================================
# ETAPA 1: Compilación con Gradle
# ============================================
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN gradle bootJar --no-daemon

# ============================================
# ETAPA 2: Imagen ligera de ejecución
# ============================================
FROM openjdk:17-slim
WORKDIR /app
RUN mkdir -p /app/uploads
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]