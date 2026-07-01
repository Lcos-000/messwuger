$ErrorActionPreference = 'Stop'

Set-Location "E:\develop\idea\collaborative project\messwuger\deploy"

docker compose -p campusassistant -f docker-compose.middleware.yml stop mysql

docker compose -p campusassistant -f docker-compose.middleware.yml rm -f mysql

docker compose -p campusassistant -f docker-compose.middleware.yml up -d mysql

docker compose -p campusassistant -f docker-compose.middleware.yml logs mysql --tail 50
