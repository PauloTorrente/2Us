# 🔥 TROUBLESHOOTING DEFINITIVO

## 🎯 SETUP INICIAL (faça EXATAMENTE nessa ordem)

### 1. DELETE a pasta antiga

```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
rm -rf coupleapp-backend
```

### 2. Extraia o novo ZIP

```bash
unzip coupleapp-backend-FINAL-SENIOR.zip
cd coupleapp-backend
```

### 3. Rode o script de startup

```bash
chmod +x start.sh
./start.sh
```

**O script vai:**
- ✅ Validar se .env existe
- ✅ Validar se todas as variáveis estão configuradas
- ✅ Verificar se porta 8080 está livre
- ✅ Compilar o projeto
- ✅ Rodar a aplicação

---

## ❌ ERRO: "cannot find symbol: class Tag"

### CAUSA:
Você está usando o ZIP antigo.

### SOLUÇÃO:
1. DELETE a pasta `coupleapp-backend` COMPLETAMENTE
2. Extraia o novo ZIP `coupleapp-backend-FINAL-SENIOR.zip`
3. Rode `./start.sh`

---

## ❌ ERRO: "Could not find artifact bucket4j"

### CAUSA:
Cache do Maven está corrompido.

### SOLUÇÃO:

```bash
# Limpar cache
rm -rf ~/.m2/repository/com/bucket4j
rm -rf ~/.m2/repository/com/github/vladimir-bukhtoyarov

# Recompilar
mvn clean package -U
```

---

## ❌ ERRO: "Failed to configure a DataSource"

### CAUSA:
Banco de dados não está acessível ou credenciais inválidas.

### SOLUÇÃO 1: Verificar .env

```bash
cat .env
```

Deve ter:

```bash
DATABASE_URL=jdbc:postgresql://ep-mute-wind-amdlzfnp-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=npg_tmNXxu8YrL5g
```

**IMPORTANTE:** URL deve começar com `jdbc:postgresql://`

### SOLUÇÃO 2: Testar conexão

```bash
psql "postgresql://neondb_owner:npg_tmNXxu8YrL5g@ep-mute-wind-amdlzfnp-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require"
```

Se não conectar, as credenciais do Neon.tech expiraram. Crie um novo banco:

1. Ir em https://neon.tech
2. Criar novo projeto
3. Copiar credenciais
4. Atualizar .env

---

## ❌ ERRO: "Port 8080 was already in use"

### SOLUÇÃO:

```bash
# Matar processo usando porta 8080
lsof -ti:8080 | xargs kill -9

# OU mudar porta no .env
echo "SERVER_PORT=8081" >> .env
```

---

## ❌ ERRO: "Process terminated with exit code: 1"

### CAUSA:
Aplicação crashou mas não mostrou o erro.

### SOLUÇÃO:

Rode com stack trace completo:

```bash
mvn spring-boot:run -e
```

OU:

```bash
java -jar target/coupleapp-backend-0.0.1-SNAPSHOT.jar 2>&1 | tee error.log
```

O erro completo vai aparecer no console.

---

## ❌ ERRO: "Caused by: javax.crypto.IllegalBlockSizeException"

### CAUSA:
JWT_SECRET muito curto (menos de 64 caracteres).

### SOLUÇÃO:

O .env já tem um JWT_SECRET válido. Se você mudou, use este:

```bash
JWT_SECRET=abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890
```

---

## ❌ ERRO: "Redis connection failed"

### CAUSA:
Redis não está acessível (normal em dev).

### SOLUÇÃO:

**Não faça nada!** O app já tem fallback para Caffeine (in-memory cache).

Mas se quiser usar Redis:

1. Ir em https://upstash.com
2. Criar banco Redis
3. Copiar credenciais
4. Atualizar .env:

```bash
REDIS_HOST=seu-host.upstash.io
REDIS_PORT=6379
REDIS_PASSWORD=sua-senha
```

---

## ✅ CHECKLIST PRÉ-COMPILAÇÃO

Antes de compilar, verifique:

- [ ] Deletou pasta `coupleapp-backend` antiga
- [ ] Extraiu `coupleapp-backend-FINAL-SENIOR.zip`
- [ ] Arquivo `.env` existe
- [ ] `DATABASE_URL` começa com `jdbc:postgresql://`
- [ ] `JWT_SECRET` tem 64+ caracteres
- [ ] Porta 8080 está livre
- [ ] Java 21+ instalado: `java -version`
- [ ] Maven instalado: `mvn -version`

---

## 🚀 COMPILAÇÃO LIMPA (reset total)

Se NADA funcionar:

```bash
# 1. Deletar tudo
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
rm -rf coupleapp-backend

# 2. Limpar cache Maven
rm -rf ~/.m2/repository/com/bucket4j
rm -rf ~/.m2/repository/com/github/vladimir-bukhtoyarov
rm -rf ~/.m2/repository/org/springdoc
rm -rf ~/.m2/repository/io/swagger

# 3. Extrair ZIP
unzip coupleapp-backend-FINAL-SENIOR.zip
cd coupleapp-backend

# 4. Compilar do ZERO
mvn clean package -U

# 5. Rodar
java -jar target/coupleapp-backend-0.0.1-SNAPSHOT.jar
```

---

## 📞 DEBUG AVANÇADO

### Ver logs completos

```bash
java -jar target/coupleapp-backend-0.0.1-SNAPSHOT.jar 2>&1 | tee app.log
```

### Ver apenas erros

```bash
mvn spring-boot:run 2>&1 | grep -i error
```

### Testar conexão banco

```bash
# Dentro do projeto
mvn spring-boot:run -Ddebug
```

Vai mostrar TODAS as configurações do Spring.

---

## 🎯 VALIDAÇÃO FINAL

Após app rodar, teste:

### 1. Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Esperado:**

```json
{
  "status": "UP"
}
```

### 2. Swagger UI

Abra no navegador:

```
http://localhost:8080/swagger-ui.html
```

Deve carregar a documentação da API.

### 3. Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste",
    "email": "teste@test.com",
    "password": "senha123"
  }'
```

**Esperado:**

```json
{
  "token": "eyJhbGci...",
  "userId": 1,
  "name": "Teste",
  "email": "teste@test.com",
  "coupleId": null
}
```

Se TODOS esses testes passarem, **o backend está 100% funcional!**

---

## 💡 DICAS

### Rodar em background

```bash
nohup java -jar target/coupleapp-backend-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### Ver logs em tempo real

```bash
tail -f app.log
```

### Parar aplicação

```bash
# Encontrar PID
ps aux | grep coupleapp-backend

# Matar processo
kill -9 PID
```

---

**🎯 99% dos erros são resolvidos deletando a pasta antiga e usando o novo ZIP!**

**Se continuar com erro, envie o output COMPLETO de:**

```bash
mvn spring-boot:run -e 2>&1 | tee error-completo.log
```
