#!/bin/bash
# Script LIMPA e COMPILA do zero - garante build limpo

echo "🗑️  Limpando target/ e classes antigas..."
rm -rf target/

echo "🔍 Verificando arquivos JWT..."
if [ -f "src/main/java/com/coupleapp/security/JwtAuthFilter.java" ]; then
    echo "❌ ERRO: JwtAuthFilter.java existe (deveria ser deletado)"
    exit 1
fi
if [ -f "src/main/java/com/coupleapp/security/JwtService.java" ]; then
    echo "❌ ERRO: JwtService.java existe (deveria ser deletado)"
    exit 1
fi

echo "🔍 Verificando properties..."
if ! grep -q "^jwt.secret=" src/main/resources/application.properties; then
    echo "❌ ERRO: jwt.secret não encontrado no properties"
    exit 1
fi
if ! grep -q "^jwt.expiration=" src/main/resources/application.properties; then
    echo "❌ ERRO: jwt.expiration não encontrado no properties"
    exit 1
fi

echo "✅ Tudo OK! Compilando..."
mvn clean package -DskipTests

echo ""
echo "✅ Build completo! Para rodar:"
echo "   mvn spring-boot:run"
