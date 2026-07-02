$ErrorActionPreference = 'Stop'

Set-Location "E:\develop\idea\collaborative project\messwuger\deploy"

docker compose -p campusassistant -f docker-compose.middleware.yml -f docker-compose.skywalking.yml up -d --remove-orphans

docker compose -p campusassistant -f docker-compose.middleware.yml -f docker-compose.skywalking.yml ps
