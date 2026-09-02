# TaskManager

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

API REST para gerenciar tarefas do usuário autenticado. É construída em Java 21, Spring Boot, Spring Security, JPA e PostgreSQL, com documentação interativa OpenAPI.

## Principais características

- Autenticação stateless por JWT Bearer: o token carrega somente o `sub` (username) e expira em 3 horas por padrão.
- Autorização por proprietário: cada operação de tarefa é limitada ao usuário autenticado; leitura de tarefa alheia retorna `404` e alteração ou exclusão retorna `403`.
- Contrato HTTP versionado em `/api/v1`, com DTOs para não expor entidades JPA.
- Categorias são gerenciadas internamente ao criar ou atualizar uma tarefa; não há endpoint próprio para elas.
- Documentação Swagger/OpenAPI disponível em [`/docs`](http://localhost:8080/docs).
- Testes unitários, HTTP, segurança, integração e arquitetura, com cobertura mínima de linhas de 90% validada pelo JaCoCo.

## Pré-requisitos

- Java 21
- Docker Desktop

## Como executar

1. Suba o PostgreSQL:

```bash
docker compose up -d
```

2. Inicie a API:

   Linux/macOS:

   ```bash
   ./mvnw spring-boot:run
   ```

   Windows:

   ```bat
   mvnw.cmd spring-boot:run
   ```

3. Abra [`http://localhost:8080/docs`](http://localhost:8080/docs). O endereço redireciona para a interface Swagger.

## Primeiro uso

1. Em `/docs`, execute `POST /api/v1/auth/registro` para criar um usuário.
2. Execute `POST /api/v1/auth/login` e copie o token retornado.
3. Clique em **Authorize** no Swagger e informe `Bearer <token>`.
4. Use os endpoints de `/api/v1/tarefas`.

## Endpoints principais

| Recurso | Endpoint | Finalidade |
|---|---|---|
| Autenticação | `POST /api/v1/auth/registro` | Registra usuário |
| Autenticação | `POST /api/v1/auth/login` | Emite JWT |
| Usuário | `GET /api/v1/usuarios/me` | Retorna o usuário do token |
| Tarefas | `GET /api/v1/tarefas` | Lista as tarefas do usuário autenticado |
| Tarefas | `POST /api/v1/tarefas` | Cria tarefa e associa/cria sua categoria |
| Tarefas | `GET /api/v1/tarefas/{id}` | Busca uma tarefa própria |
| Tarefas | `PUT /api/v1/tarefas/{id}` | Atualiza uma tarefa própria |
| Tarefas | `PATCH /api/v1/tarefas/{id}/concluir` | Conclui uma tarefa própria |
| Tarefas | `PATCH /api/v1/tarefas/{id}/reabrir` | Reabre uma tarefa própria |
| Tarefas | `DELETE /api/v1/tarefas/{id}` | Exclui uma tarefa própria |

Exceto por registro, login e documentação, todos os endpoints exigem o cabeçalho `Authorization: Bearer <token>`.

## Variáveis de ambiente

| Variável | Valor padrão | Descrição |
|----------|-------------|-----------|
| `DB_USERNAME` | `postgres` | Usuário do PostgreSQL |
| `DB_PASSWORD` | `postgres` | Senha do PostgreSQL |
| `POSTGRES_DB` | `taskmanager` | Nome do banco de dados criado pelo container |
| `POSTGRES_USER` | `postgres` | Usuário do container PostgreSQL |
| `POSTGRES_PASSWORD` | `postgres` | Senha do container PostgreSQL |
| `POSTGRES_PORT` | `5433` | Porta exposta do PostgreSQL no host |
| `JWT_SECRET` | chave de desenvolvimento | Segredo para assinatura dos tokens; defina um valor forte fora do desenvolvimento |
| `JWT_EXPIRATION` | `10800000` | Duração do token, em milissegundos (3 horas) |

> A configuração atual usa `spring.jpa.hibernate.ddl-auto=create-drop`: o schema é criado ao iniciar e removido ao encerrar a aplicação. É apropriada para desenvolvimento, não para produção.

## Recriar o banco do zero

Para remover todos os dados e recriar o banco:

```bash
docker compose down -v
docker compose up -d
```

## Testes

Linux/macOS:

```bash
./mvnw test
```

Windows:

```bat
mvnw.cmd test
```

O comando executa a suíte com H2 em memória e falha se a cobertura de linhas ficar abaixo de 90%.

## Estrutura e documentação técnica

- `src/main/java/com/example/taskmanager/` — API, organizada em controllers, services, repositories, entities, DTOs, mappers e segurança.
- `src/test/java/` — testes de comportamento e de arquitetura.
- `docs/Modulo_Avancado/specs/` — especificações vivas dos módulos.
- `docs/Modulo_Avancado/adr/` — decisões arquiteturais registradas.
- `docker-compose.yml` — PostgreSQL 17
