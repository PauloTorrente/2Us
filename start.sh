#!/bin/bash

echo "🚀 CoupleApp Backend - Startup Script"
echo "======================================"
echo ""

# Verificar se .env existe
if [ ! -f .env ]; then
    echo "❌ ERRO: Arquivo .env não encontrado!"
    echo "Crie um arquivo .env na raiz do projeto"
    exit 1
fi

echo "✅ Arquivo .env encontrado"

# Carregar .env
source .env 2>/dev/null || true

# Verificar variáveis críticas
MISSING_VARS=()

if [ -z "$DATABASE_URL" ]; then
    MISSING_VARS+=("DATABASE_URL")
fi

if [ -z "$DATABASE_USERNAME" ]; then
    MISSING_VARS+=("DATABASE_USERNAME")
fi

if [ -z "$DATABASE_PASSWORD" ]; then
    MISSING_VARS+=("DATABASE_PASSWORD")
fi

if [ -z "$JWT_SECRET" ]; then
    MISSING_VARS+=("JWT_SECRET")
fi

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    echo "❌ ERRO: Variáveis faltando no .env:"
    for var in "${MISSING_VARS[@]}"; do
        echo "   - $var"
    done
    exit 1
fi

echo "✅ Todas as variáveis obrigatórias configuradas"

# Verificar se porta está em uso
PORT=${SERVER_PORT:-8080}
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  AVISO: Porta $PORT já está em uso"
    echo "Processo usando a porta:"
    lsof -i :$PORT
    echo ""
    echo "Para matar o processo:"
    echo "  lsof -ti:$PORT | xargs kill -9"
    exit 1
fi

echo "✅ Porta $PORT disponível"
echo ""

# Compilar
echo "📦 Compilando projeto..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ ERRO na compilação!"
    exit 1
fi

echo ""
echo "✅ Compilação concluída!"
echo ""
echo "🚀 Iniciando aplicação..."
echo "======================================"
echo "API: http://localhost:$PORT/api"
echo "Swagger: http://localhost:$PORT/swagger-ui.html"
echo "Health: http://localhost:$PORT/actuator/health"
echo "======================================"
echo ""

# Rodar aplicação
java -jar target/coupleapp-backend-0.0.1-SNAPSHOT.jar
