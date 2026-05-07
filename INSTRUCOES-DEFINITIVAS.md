# 🚀 INSTRUÇÕES DEFINITIVAS - COUPLEAPP BACKEND

## ⚠️ PROBLEMA MAIS COMUM: VOCÊ ESTÁ NO DIRETÓRIO ERRADO!

### ❌ ERRADO (gera erro "No plugin found for prefix 'spring-boot'"):
```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
mvn spring-boot:run
```

### ✅ CORRETO:
```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/coupleapp-backend/
mvn spring-boot:run
```

---

## 📋 PASSO A PASSO COMPLETO (COPIE E COLE):

### 1. DELETAR TUDO ANTIGO:
```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
rm -rf coupleapp-backend
```

### 2. EXTRAIR O ZIP:
```bash
# No Windows Explorer, clique com botão direito no coupleapp-backend-DEFINITIVO.zip
# Escolha "Extract All..." ou "Extrair tudo..."
# Extraia para: C:\Users\Trabalho\Documents\Aplicativo de casal\
```

**OU via terminal WSL:**
```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
unzip -o coupleapp-backend-DEFINITIVO.zip
```

### 3. ENTRAR NO DIRETÓRIO CORRETO:
```bash
cd coupleapp-backend
pwd  # Deve mostrar: /mnt/c/Users/Trabalho/Documents/Aplicativo de casal/coupleapp-backend
```

### 4. VERIFICAR SE ESTÁ NO LUGAR CERTO:
```bash
ls pom.xml  # Deve mostrar: pom.xml
```

Se der erro "No such file or directory", VOCÊ ESTÁ NO LUGAR ERRADO!

### 5. RODAR:
```bash
mvn spring-boot:run
```

---

## ✅ VERIFICAÇÃO COMPLETA DO CÓDIGO (100% OK):

### Arquivos JWT (3 corretos):
- ✅ `JwtUtil.java`
- ✅ `JwtAuthenticationFilter.java`  
- ✅ `CustomUserDetailsService.java`

### Propriedades (8/8 mapeadas):
| @Value no código | application.properties |
|------------------|------------------------|
| `jwt.secret` | ✅ |
| `jwt.expiration` | ✅ |
| `app.cache.ttl-hours:1` | ✅ |
| `spring.data.redis.host` | ✅ |
| `spring.data.redis.port` | ✅ |
| `spring.data.redis.password:` | ✅ |
| `app.google.places.api-key` | ✅ |
| `app.tripadvisor.api-key` | ✅ |

### Compatibilidade:
- ✅ Java 21
- ✅ Spring Boot 3.2.4
- ✅ User implements UserDetails
- ✅ JwtUtil.generateToken(UserDetails)
- ✅ CoupleStatus enum completo
- ✅ Couple entity com todos os campos
- ✅ Sem imports de arquivos deletados
- ✅ .env com todas variáveis

---

## 🔥 SE DER ERRO, É PORQUE:

### 1. Você está no diretório errado
**Sintoma:** `No plugin found for prefix 'spring-boot'`

**Solução:**
```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/coupleapp-backend/
pwd  # Confirme que está aqui!
```

### 2. Não extraiu o ZIP direito
**Sintoma:** `pom.xml not found`

**Solução:**
```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
rm -rf coupleapp-backend
unzip -o coupleapp-backend-DEFINITIVO.zip
cd coupleapp-backend
```

### 3. Tem código antigo misturado
**Sintoma:** `Could not resolve placeholder 'app.jwt.expiration-ms'`

**Solução:**
```bash
# DELETAR TUDO e extrair novamente
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/
rm -rf coupleapp-backend
unzip -o coupleapp-backend-DEFINITIVO.zip
```

---

## 📊 RESULTADO ESPERADO:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2026-05-XX XX:XX:XX INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http)
2026-05-XX XX:XX:XX INFO  com.coupleapp.CoupleAppApplication - Started CoupleAppApplication in X.XXX seconds
```

---

## 🎯 COMANDO ÚNICO (COPIE TUDO):

```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/ && \
rm -rf coupleapp-backend && \
unzip -o coupleapp-backend-DEFINITIVO.zip && \
cd coupleapp-backend && \
mvn clean package -DskipTests && \
mvn spring-boot:run
```

---

## ❓ SE AINDA DER ERRO, MANDE:

```bash
cd /mnt/c/Users/Trabalho/Documents/Aplicativo\ de\ casal/coupleapp-backend/
pwd
ls -la | head -20
cat src/main/resources/application.properties | head -30
```
