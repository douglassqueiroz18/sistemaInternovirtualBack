#!/bin/bash

# Configurações de cores para o terminal
GREEN='\033[0-32m'
RED='\033[0-31m'
NC='\033[0m' # No Color

echo -e "${GREEN}===> Iniciando Limpeza Geral...${NC}"
docker compose down
docker image rm app-backend 2>/dev/null || true

# Garante que a rede exista
docker network inspect app-network >/dev/null 2>&1 || \
    docker network create app-network

echo -e "${GREEN}===> Subindo Containers (Banco e Backend)...${NC}"
# Usamos --build para garantir que o backend seja recompilado
docker compose up -d --build

echo -e "${GREEN}===> Aguardando o Banco de Dados (Postgres)...${NC}"
# Loop de teste de conexão simples
for i in {1..10}; do
  if docker exec app-backend nc -z postgres_db 5432; then
    echo -e "${GREEN}Postgres está online!${NC}"
    break
  fi
  echo "Tentando conectar ao banco ($i/10)..."
  sleep 3
done

echo -e "${GREEN}===> Testando Health Check do Backend...${NC}"
sleep 5 # Tempo para o Spring dar o boot
status_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health || echo "000")

if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 404 ]; then
  echo -e "${GREEN}SUCESSO: Backend está respondendo na porta 8080!${NC}"
else
  echo -e "${RED}FALHA: O Backend retornou status $status_code. Verifique os logs com 'docker logs app-backend'${NC}"
fi

echo -e "${GREEN}Processo concluído.${NC}"