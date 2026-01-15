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

Se quiser rodar a aplicação Spring Boot localmente via IDE:

1. Inicie apenas os serviços de infraestrutura:
   ```bash
   docker-compose up -d db redis localstack
   ```

2. Configure o `.envrc` no diretório `back/` apontando para localhost:
   ```bash
   export DB_HOST='jdbc:postgresql://localhost:5435/instrutoresbrasil'
   export REDIS_HOST='localhost'
   export AWS_ENDPOINT_OVERRIDE='http://localhost:4566'
   ```

3. Execute a aplicação Spring Boot via IDE ou Maven.

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
