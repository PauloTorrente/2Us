#!/bin/bash

# Script de validação do código antes de compilar
# Garante que não há erros comuns

echo "🔍 Verificando código..."

# 1. Verificar imports do Swagger
echo "✓ Verificando imports Swagger @Tag..."
BAD_IMPORTS=$(grep -r "import io.swagger.v3.oas.annotations.Tag;" src/main/java/ 2>/dev/null || true)
if [ -n "$BAD_IMPORTS" ]; then
    echo "❌ ERRO: Import errado encontrado!"
    echo "$BAD_IMPORTS"
    echo "Deve ser: import io.swagger.v3.oas.annotations.tags.Tag;"
    exit 1
fi

# 2. Verificar se pom.xml tem Bucket4j correto
echo "✓ Verificando Bucket4j no pom.xml..."
if grep -q "com.github.vladimir-bukhtoyarov" pom.xml; then
    echo "❌ ERRO: groupId do Bucket4j está errado!"
    echo "Deve ser: com.bucket4j"
    exit 1
fi

# 3. Verificar versão do Swagger
echo "✓ Verificando versão Swagger..."
SWAGGER_VERSION=$(grep -A2 "springdoc-openapi-starter-webmvc-ui" pom.xml | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
if [ "$SWAGGER_VERSION" != "2.6.0" ]; then
    echo "⚠️  AVISO: Versão do Swagger é $SWAGGER_VERSION (recomendado: 2.6.0)"
fi

# 4. Verificar se .env existe
echo "✓ Verificando .env..."
if [ ! -f .env ]; then
    echo "❌ ERRO: Arquivo .env não encontrado!"
    echo "Copie .env.example para .env e configure as variáveis"
    exit 1
fi

# 5. Verificar variáveis críticas no .env
echo "✓ Verificando variáveis do .env..."
source .env 2>/dev/null || true

if [ -z "$DATABASE_URL" ]; then
    echo "⚠️  AVISO: DATABASE_URL não configurado"
fi

if [ -z "$GOOGLE_PLACES_API_KEY" ]; then
    echo "⚠️  AVISO: GOOGLE_PLACES_API_KEY não configurado"
fi

if [ -z "$REDIS_PASSWORD" ]; then
    echo "⚠️  AVISO: REDIS_PASSWORD não configurado"
fi

echo ""
echo "✅ Validação concluída!"
echo ""
echo "Agora rode:"
echo "  mvn clean package"
echo ""
