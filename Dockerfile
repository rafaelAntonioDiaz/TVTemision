FROM eclipse-temurin:25-jre
WORKDIR /app
RUN mkdir -p /app/uploads
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]