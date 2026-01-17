# InstrutorBrasil

Guia para rodar o backend usando Docker.

## Pré-requisitos

- Docker instalado
- Docker Compose instalado

## Quick Start

### 1. Configure os arquivos necessários

Copie os arquivos de exemplo:

```bash
# Configuração do Docker
cd docker/
cp .env.example .env

# Configuração da aplicação Spring Boot
cd ../back/src/main/resources/
cp application.yml.example application.yml
```

Edite o arquivo `docker/.env` e configure:
- `DB_PASSWORD`: Senha do banco de dados
- `JWT_SECRET`: Chave secreta para JWT (gere com: `openssl rand -base64 32`)
- `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET`: Credenciais OAuth2 do Google (Opcional)

As configurações padrão já funcionam para desenvolvimento local.

### 2. Inicie os serviços

No diretório `docker/`, execute:

```bash
docker compose up -d
```

### 3. Verifique se está funcionando

Aguarde alguns segundos para a aplicação iniciar, então acesse:
http://localhost:8080/swagger-ui/index.html


## Serviços

| Serviço        | Porta Externa | Descrição                      |
|----------------|---------------|--------------------------------|
| **PostgreSQL** | 5435          | Banco de dados relacional      |
| **Redis**      | 6379          | Cache e rate limiting          |
| **LocalStack** | 4566          | Simulador AWS S3 (desenvolvimento) |
| **Spring Boot** | 8080         | Aplicação backend              |

## Comandos úteis

### Ver logs
```bash
# Todos os serviços
docker compose logs -f

# Serviço específico
docker compose logs -f app
```

### Parar os serviços
```bash
docker-compose down
```

### Reconstruir a aplicação após mudanças no código
```bash
docker-compose up -d --build app
```

### Iniciar apenas alguns serviços
```bash
# Apenas infraestrutura (para rodar a app localmente)
docker-compose up -d db redis localstack
```

## Conectando ao banco de dados

### Do host (sua máquina)
```
Host: localhost
Port: 5435
Database: instrutoresbrasil
Username: postgres
Password: (definido no .env)
```

### Via psql
```bash
docker exec -it instrutoresbrasil-database psql -U postgres -d instrutoresbrasil
```

## LocalStack (S3 Simulado)

O LocalStack simula o AWS S3 localmente:

### Criar bucket
```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://instrutorbrasil-documents
```

### Listar buckets
```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

## Desenvolvimento local (sem Docker para a app)

Se quiser rodar a aplicação Spring Boot localmente via IDE/Maven (fora do Docker) enquanto usa o Docker Compose apenas para os serviços auxiliares:

### 1. Inicie apenas os serviços de infraestrutura

No diretório `docker/`, execute:

```bash
docker compose up -d db redis localstack
```

Isso irá iniciar apenas PostgreSQL, Redis e LocalStack. O Spring Boot rodará na sua máquina.

### 2. Configure o application.yml manualmente

Como não teremos o arquivo `.env` populando as variáveis automaticamente, você precisa editar manualmente o arquivo `back/src/main/resources/application.yml`.

**Diferenças importantes:**
- **Dentro do Docker**: os serviços se comunicam pelos nomes dos containers (`db`, `redis`, `localstack`) na porta interna
- **Fora do Docker**: você acessa via `localhost` nas portas externas mapeadas

Substitua as variáveis de ambiente pelas configurações abaixo:

```yaml
spring:
  application:
    name: instrutorbrasil

  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5435/instrutoresbrasil
    username: postgres
    password: postgres  # Mesma senha definida no docker/.env
    driver-class-name: org.postgresql.Driver

  # JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # Email Configuration
  mail:
    host: smtp.example.com
    port: 587
    username: no-reply@example.com
    password: password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
    test:
      connection: false
    mock:
      enabled: true  # true para desenvolvimento (logs no console)

  # OAuth2 Google Configuration (Opcional)
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: google-client-id
            client-secret: google-client-secret
            scope: profile,email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

  # Thymeleaf Configuration
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    cache: false

  # Redis Configuration
  data:
    redis:
      host: localhost  # localhost ao invés de 'redis'
      port: 6379
      password: password  # Mesma senha definida no docker/.env
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

  # Async Configuration
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100

# Server Configuration
server:
  forward-headers-strategy: native
  port: 8080

# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method

# JWT Configuration
jwt:
  secret: change-me-please  # Use: openssl rand -base64 32
  access-token-expiration: 900000
  refresh-token-expiration: 2592000000

# Application Configuration
app:
  email:
    from: no-reply@example.com
    from-name: InstrutorBrasil
  frontend:
    url: http://localhost:3000
  token:
    password-reset-expiration: 3600000
    email-verification-expiration: 86400000
  rate-limit:
    storage-type: REDIS  # ou MEMORY para desenvolvimento simples

  # AWS S3 Configuration (LocalStack)
  aws:
    region: us-east-1
    s3:
      bucket: instrutorbrasil-documents
      presigned-url-expiration-minutes: 15
      max-file-size-mb: 10
      access-key-id: test
      secret-access-key: test
      endpoint-override: http://localhost:4566  # localhost ao invés de 'localstack'

# Logging Configuration
logging:
  level:
    kaiquebt.dev.instrutorbrasil: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
```

### 3. Execute a aplicação Spring Boot

Via Maven:
```bash
cd back/
./mvnw spring-boot:run
```

Via IDE:
- Abra o projeto em sua IDE (IntelliJ IDEA, Eclipse, VS Code)
- Execute a classe principal com `@SpringBootApplication`

### 4. Acesse a aplicação

http://localhost:8080/swagger-ui/index.html

### Resumo das configurações importantes

| Configuração | Valor no Docker | Valor Local (fora do Docker) |
|-------------|----------------|------------------------------|
| `datasource.url` | `jdbc:postgresql://db:5432/...` | `jdbc:postgresql://localhost:5435/...` |
| `redis.host` | `redis` | `localhost` |
| `aws.endpoint-override` | `http://localstack:4566` | `http://localhost:4566` |

## Troubleshooting

### A aplicação não inicia
- Verifique os logs: `docker-compose logs -f app`
- Verifique se todas as portas estão disponíveis (8080, 5435, 6379, 4566)
- Verifique as variáveis de ambiente no `.env`

### Erro de conexão com banco de dados
- Certifique-se de que o serviço está rodando: `docker-compose ps db`
- Aguarde alguns segundos após o `docker-compose up` para o banco estar pronto

### Limpar tudo e recomeçar
```bash
docker-compose down -v
rm -rf postgres redis .localstack
docker-compose up -d
```

## Notas

- A porta PostgreSQL externa é **5435** (não 5432) para evitar conflitos com instalações locais
  - **Importante**: Dentro da rede Docker, use a porta **5432** (porta interna)
  - Use a porta 5435 apenas ao conectar do host (sua máquina)
- LocalStack usa credenciais `test`/`test` - não use em produção
- Os dados são persistidos em: `./postgres/`, `./redis/` e `./.localstack/`
