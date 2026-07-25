# Design: DTOs para a API TaskManager

**Data:** 2026-07-23

## Objetivo

Definir todos os DTOs (Data Transfer Objects) de request e response para a API RESTful do TaskManager, incluindo validações Bean Validation, e atualizar o AGENTS.md com a regra de que Bean Validation é obrigatório em todos os DTOs.

## DTOs

### TarefaRequestDTO

Usado para **POST** `/tarefas` (criação) e **PUT** `/tarefas/{id}` (atualização completa). O service layer é responsável por aplicar as validações de negócio e determinar quais campos foram alterados no caso de PUT.

| Campo | Tipo | Validação | Observação |
|-------|------|-----------|------------|
| titulo | String | `@NotBlank`, `@Size(max=70)` | Título da tarefa |
| descricao | String | `@Size(max=255)`, nullable | Descrição detalhada (opcional) |
| categoriaNome | String | `@NotBlank` | Nome da categoria (service busca/cria internamente) |

**Nota:** `usuarioId` NÃO deve estar neste DTO. O usuário é obtido via JWT/SecurityContext no service layer.

### TarefaResponseDTO

Usado para **GET** `/tarefas` (listagem) e **GET** `/tarefas/{id}` (detalhe). Controllers NUNCA retornam entidades JPA diretamente.

| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long | ID da tarefa |
| titulo | String | Título da tarefa |
| descricao | String | Descrição detalhada |
| concluida | Boolean | Status de conclusão |
| dataCriacao | LocalDateTime | Data de criação (auto-preenchida) |
| usuarioId | Long | ID do dono (não expõe objeto Usuario completo) |
| categoriaId | Long | ID da categoria |
| categoriaNome | String | Nome da categoria (denormalização para conveniência do frontend) |

### LoginRequest

Usado para **POST** `/auth/login`.

| Campo | Tipo | Validação |
|-------|------|-----------|
| username | String | `@NotBlank` |
| senha | String | `@NotBlank` |

### LoginResponse

Usado para **POST** `/auth/login`. Conforme AGENTS.md: "Apenas token e nome — sem outros dados".

| Campo | Tipo | Observação |
|-------|------|------------|
| token | String | JWT assinado |
| nome | String | Nome real do usuário |

### RegistroRequest

Usado para **POST** `/auth/registro`.

| Campo | Tipo | Validação | Observação |
|-------|------|-----------|------------|
| nome | String | `@NotBlank`, `@Size(max=75)` | Nome real do usuário |
| username | String | `@Size(min=8, max=15)` | Identificador único para login (tamanho impede blank) |
| senha | String | `@NotBlank`, `@Size(min=6)` | Senha em texto plano (service aplica BCrypt) |

**Nota:** RequestDTO de senha NUNCA é reaproveitado como ResponseDTO — senha jamais aparece em nenhuma resposta da API.

### UsuarioResponseDTO

Usado para **GET** `/usuarios/me`.

| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long | ID do usuário autenticado |
| nome | String | Nome real |
| username | String | Identificador de login |

## Decisões de Implementação

### Regras de validação Bean Validation
- **Obrigatória** em todos os DTOs — regra a ser adicionada ao AGENTS.md
- RequestDTO nunca inclui `id` nem campos calculados/derivados pelo servidor
- RequestDTO de senha nunca é reaproveitado como ResponseDTO
- Todo campo obrigatório em RequestDTO deve ter Bean Validation refletindo as constraints do schema do banco

### Escopo de arquivos por entidade
- **Categoria:** apenas Entity e Repository — **sem DTOs próprios**. É gerenciada internamente pelo TarefaService (busca/cria categoria a partir do repository, sem expor endpoint dedicado).
- **Usuario:** conjunto completo (Entity, Repository, Service, Controller, DTOs)
- **Tarefa:** conjunto completo (Entity, Repository, Service, Controller, DTOs)

### PATCH `/tarefas/{id}/concluir`
- Endpoint de ação pontual: **sem request body**
- O controller não recebe `@RequestBody`
- A ação é determinada inteiramente pelo path (`id` da tarefa)
- HTTP PATCH com body vazio é válido — não afeta o verbo

### Mesmo DTO para POST e PUT de tarefas
- `TarefaRequestDTO` é reutilizado para criação e atualização
- Service layer decide quais campos aplicar no caso de PUT (campos divergentes)

### Referências em ResponseDTOs
- `TarefaResponseDTO` referencia `Usuario` e `Categoria` apenas por `id` (Long), nunca pelo objeto completo
- `categoriaNome` é incluído por denormalização (conveniência para o frontend)

### Fluxo de autenticação e usuarioId
- O `usuarioId` em operações de tarefas vem do JWT/SecurityContext, nunca do corpo da requisição
- Não deve existir rota como `/tarefas?usuarioId=X` ou `/usuarios/{id}/tarefas`
- `RegistroRequest` NÃO inclui `usuarioNome` — o usuário autenticado é obtido via token

## Arquivos a Criar

1. `src/main/java/com/example/taskmanager/dto/TarefaRequestDTO.java`
2. `src/main/java/com/example/taskmanager/dto/TarefaResponseDTO.java`
3. `src/main/java/com/example/taskmanager/dto/LoginRequest.java`
4. `src/main/java/com/example/taskmanager/dto/LoginResponse.java`
5. `src/main/java/com/example/taskmanager/dto/RegistroRequest.java`
6. `src/main/java/com/example/taskmanager/dto/UsuarioResponseDTO.java`

## Testes

- Testar validações Bean Validation (campos obrigatórios, tamanhos máximos/mínimos)
- Testar que ResponseDTOs não expõem campos sensíveis (senha)
- Testar que RequestDTOs não aceitam `id`

## Fora do Escopo

- Repositories, Services, Controllers, Security — definidos em spec separada
- Migração do banco de dados
- Configuração Swagger/OpenAPI
