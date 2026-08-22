# TaskManager

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

API REST para gerenciamento de tarefas, usuários e categorias, desenvolvida em Java com Spring Boot e PostgreSQL.

## Pré-requisitos

- Java 21
- Docker Desktop

## Como executar

### 1. Iniciar o PostgreSQL

```bash
docker compose up -d
```

### 2. Compilar a aplicação

```bash
mvnw.cmd -DskipTests compile
```

### 3. Executar a aplicação

```bash
mvnw.cmd spring-boot:run
```

## Variáveis de ambiente

| Variável | Valor padrão | Descrição |
|----------|-------------|-----------|
| `DB_USERNAME` | `postgres` | Usuário do PostgreSQL |
| `DB_PASSWORD` | `postgres` | Senha do PostgreSQL |
| `POSTGRES_DB` | `taskmanager` | Nome do banco de dados criado pelo container |
| `POSTGRES_USER` | `postgres` | Usuário do container PostgreSQL |
| `POSTGRES_PASSWORD` | `postgres` | Senha do container PostgreSQL |
| `POSTGRES_PORT` | `5432` | Porta exposta do container PostgreSQL |

## Recriar o banco do zero

Para remover todos os dados e recriar o banco:

```bash
docker compose down -v
docker compose up -d
```

## Estrutura do projeto

- `src/main/java/com/example/taskmanager/` — código-fonte da API
  - `entity/` — entidades JPA
  - `repository/` — repositórios Spring Data
  - `service/` — regras de negócio
  - `controller/` — endpoints REST
  - `dto/` — objetos de transferência de dados
  - `config/` — configurações da aplicação
- `src/main/resources/` — propriedades e configurações
- `src/test/java/` — testes automatizados (H2 em memória)
- `docker-compose.yml` — PostgreSQL 17
