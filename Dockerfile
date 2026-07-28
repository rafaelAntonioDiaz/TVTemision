# ============================================
# ETAPA 1: Compilación con Gradle y JDK 25
# ============================================
FROM eclipse-temurin:25-jre-alpine AS build
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copiar el código fuente
COPY src src

# Dar permisos de ejecución al wrapper
RUN chmod +x gradlew

# Compilar y empaquetar la aplicación (omitir tests para acelerar)
RUN ./gradlew bootJar --no-daemon -x test

# ============================================
# ETAPA 2: Imagen ligera de ejecución con JRE 25
# ============================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Crear directorio para archivos subidos
RUN mkdir -p /app/uploads

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando de inicio (activa el perfil 'prod' si lo tienes)
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]