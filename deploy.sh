#!/bin/bash
echo "🚀 Compilando y desplegando Geo Petro Litix AI..."

# Compilar localmente (sin tests para acelerar)
./gradlew bootJar -x test

# Subir el JAR a la VM
scp build/libs/EmisionTVT_FF-0.0.1-SNAPSHOT.jar rafael_diaz@34.66.125.104:/home/rafael_diaz/TVTemision/app.jar

# Conectar a la VM, hacer pull por si acaso y reconstruir solo el servicio 'app'
ssh rafael_diaz@34.66.125.104 "cd /home/rafael_diaz/TVTemision && git pull origin main && docker compose up -d --build app"

echo "✅ Despliegue completado en https://geopetrolitixai.pro"