# Docker Setup - Instrutor Brasil

Este diretório contém a configuração Docker para o projeto Instrutor Brasil.

## Serviços

O `docker-compose.yml` configura os seguintes serviços:

| Serviço        | Porta Externa | Porta Interna | Descrição                      |
|----------------|---------------|---------------|--------------------------------|
| **PostgreSQL** | 5435          | 5432          | Banco de dados relacional      |
| **Redis**      | 6379          | 6379          | Cache e rate limiting          |
| **LocalStack** | 4566          | 4566          | Simulador AWS S3 (desenvolvimento) |
| **Spring Boot** | 8080         | 8080          | Aplicação backend              |

## Configuração Inicial

1. **Copie o arquivo de variáveis de ambiente:**
   ```bash
   cp .env.example .env
   ```

2. **Edite o arquivo `.env` e configure:**
   - `POSTGRES_PASSWORD`: Senha do banco de dados
   - `JWT_SECRET`: Chave secreta para JWT (gere com: `openssl rand -base64 32`)
   - `GOOGLE_CLIENT_ID`: ID do cliente OAuth2 do Google
   - `GOOGLE_CLIENT_SECRET`: Secret do cliente OAuth2 do Google

## Uso

### Iniciar todos os serviços

```bash
docker-compose up -d
```

### Iniciar serviços específicos

```bash
# Apenas banco de dados e Redis
docker-compose up -d db redis

# Apenas a aplicação (requer db, redis e localstack)
docker-compose up -d app
```

### Ver logs

```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f app
docker-compose logs -f db
```

### Parar os serviços

```bash
docker-compose down
```

### Parar e remover volumes (CUIDADO: apaga dados do banco)

```bash
docker-compose down -v
```

### Rebuild da aplicação

```bash
# Rebuild e reinicia o serviço
docker-compose up -d --build app
```

## Estrutura de Volumes

Os dados são persistidos nos seguintes diretórios:

- `./postgres/`: Dados do PostgreSQL
- `./redis/`: Dados do Redis
- `./.localstack/`: Dados do LocalStack (S3 simulado)

## Health Checks

A aplicação Spring Boot possui health check configurado em:
- **Endpoint**: `http://localhost:8080/actuator/health`
- **Intervalo**: 30 segundos
- **Start Period**: 40 segundos (tempo para a aplicação iniciar)

Você pode verificar o status com:

```bash
docker-compose ps
```

## Conectando ao banco de dados

### De dentro do container Spring Boot:
```
Host: db
Port: 5432
```

### Do host (sua máquina):
```
Host: localhost
Port: 5435
Database: instrutoresbrasil
Username: postgres (configurável no .env)
Password: (definido no .env)
```

## LocalStack (S3 Simulado)

Para desenvolvimento local, o LocalStack simula o AWS S3:

- **Endpoint**: `http://localhost:4566`
- **Region**: `us-east-1`
- **Credentials**: `test` / `test`

### Criar bucket no LocalStack:

```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://instrutorbrasil-documents
```

### Listar buckets:

```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

## Troubleshooting

### A aplicação não inicia

1. Verifique os logs: `docker-compose logs -f app`
2. Verifique se o banco está pronto: `docker-compose ps db`
3. Verifique as variáveis de ambiente no `.env`

### Erro de conexão com banco de dados

- Certifique-se de que o serviço `db` está rodando: `docker-compose ps`
- Verifique as credenciais no `.env`

### Rebuild necessário

Se você alterou código Java ou dependências Maven:

```bash
docker-compose up -d --build app
```

### Limpar tudo e recomeçar

```bash
docker-compose down -v
rm -rf postgres redis .localstack
docker-compose up -d
```

## Desenvolvimento

### Modo de desenvolvimento (sem container da app)

Se você quiser rodar a aplicação Spring Boot localmente (fora do Docker) mas usar os outros serviços:

1. Inicie apenas os serviços de infraestrutura:
   ```bash
   docker-compose up -d db redis localstack
   ```

2. Configure o `.envrc` no diretório `back/` com:
   ```bash
   export DB_HOST='jdbc:postgresql://localhost:5435/instrutoresbrasil'
   export REDIS_HOST='localhost'
   export AWS_ENDPOINT_OVERRIDE='http://localhost:4566'
   ```

3. Execute a aplicação Spring Boot normalmente via IDE ou Maven.

## Notas

- A porta PostgreSQL externa é **5435** (não 5432) para evitar conflitos com PostgreSQL instalado no host
- Redis não possui senha configurada por padrão
- LocalStack usa credenciais `test`/`test` - não use em produção
- O rate limiting está configurado para usar Redis por padrão
