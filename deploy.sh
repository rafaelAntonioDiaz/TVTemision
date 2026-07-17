#!/bin/bash

echo "🚀 Iniciando despliegue de Geo-IA TVT..."

# Ir al directorio del proyecto
cd /opt/tvt-platform  # ajusta la ruta en tu VM

# Obtener últimos cambios
git pull origin main

# Construir y levantar contenedores
docker-compose build app
docker-compose up -d

echo "✅ Despliegue completado. La aplicación está corriendo en http://localhost:8080"