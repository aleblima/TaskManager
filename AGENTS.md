# TaskManager — Migração para API RESTful (Spring Boot)

## Arquitetura (geral)

Migração de aplicação local (JavaFX + SQLite) para API RESTful, stateless e 
multiusuário com Spring Boot. Nesse arquivo contém todas as regras do 
projeto que não devem ser quebradas **NUNCA**!

### Stack tecnológica
| Camada | Tecnologia |
|--------|-----------|
| Framework | Spring Boot 4.1.0 (Java 21) |
| Persistência | JPA / Hibernate (`ddl-auto=create-drop`) |
| Banco principal | PostgreSQL |
| Banco de testes | H2 em memória |
| Autenticação | JWT (jjwt 0.12.6) |
| Documentação | Swagger (springdoc-openapi 2.5.0) |
| Build | Maven |

### Estrutura de pacotes (em `com.example.taskmanager`)
```
controller/    → REST controllers (endpoints)
service/       → Lógica de negócio
entity/        → JPA entities
repository/    → Spring Data JPA repositories
dto/           → Data Transfer Objects (payloads de request/response)
exception/     → Tratamento global de exceções (@ControllerAdvice)
security/      → Filtros JWT, SecurityConfig, UserDetailsService
config/        → Outras configurações (Swagger, etc.)
```

### Regras de tratamento de erros
- Erros de validação, dados não encontrados, conflitos, acesso negado → usar `@ControllerAdvice` global
- Login com credenciais inválidas → `401`
- Token ausente, inválido ou expirado → `401`
- Reaproveitar lógica de negócio existente da branch `Main`

---

## Modelagem dos dados

### Entidades

#### `Categoria`
| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long (PK) | Gerado automaticamente |
| nome | String | **Case-insensitive**: "trabalho" e "Trabalho" são a mesma categoria |

#### `Usuario`
| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long (PK) | Gerado automaticamente |
| nome | String | Nome real do usuário |
| username | String | Identificador único para login |
| senha | String | Armazenar com BCrypt |

#### `Tarefa`
| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long (PK) | Gerado automaticamente |
| titulo | String | Título da tarefa (obrigatório) |
| descricao | String | Descrição detalhada (opcional) |
| concluida | Boolean | Status da tarefa (padrão: false) |
| dataCriacao | LocalDateTime | Data de criação (auto-preenchida) |
| usuario | Usuario | `@ManyToOne` — dono da tarefa |
| categoria | Categoria | `@ManyToOne` — gerenciada pela tarefa (sem CRUD separado de categorias) |

## DTOs e Relacionamentos
- Relacionamentos `Tarefa→Usuario` e `Tarefa→Categoria` são unidirecionais
  (`@ManyToOne` apenas); não adicionar `@OneToMany` do lado inverso em
  `Usuario` ou `Categoria`.
- Consultas de listas (ex: tarefas de um usuário) são feitas via métodos
  de query no Repository, nunca por navegação de entidade
  (ex: `usuario.getTarefas()` não existe).
- Controllers nunca retornam entidades JPA diretamente; sempre DTOs.
- TarefaResponseDTO expõe apenas os dados comuns, porém em Usuario e Categoria somente 
  os seus IDs(long) e nomes, não o objeto completo.
- O `usuarioId` usado em qualquer consulta/filtro de tarefas vem sempre do
  usuário autenticado (via JWT/SecurityContext), nunca de parâmetro de URL
  ou corpo da requisição — não deve existir rota como
  `/tarefas?usuarioId=X` ou `/usuarios/{id}/tarefas`.
- Bean Validation deve ser mantida em todos os DTOs, uma regra indiscutivel.

## Escopo de arquivos por entidade
- Categoria possui apenas Entity e Repository — sem Controller, Service ou
  DTO próprios. É gerenciada internamente pelo TarefaService (busca/cria
  categoria a partir do repository, sem expor endpoint dedicado).
- Usuario e Tarefa possuem o conjunto completo de camadas
  (Entity, Repository, Service, Controller, DTOs).

## Estilo de desenvolvimento
- Services (e demais componentes com múltiplas implementações possíveis)
  são desenvolvidos contra interfaces, seguindo o padrão já usado na
  branch Main (ex: TarefaServiceInterface, UsuarioServiceInterface).

### Configuração JPA
- `ddl-auto=create-drop` — schema recriado a cada inicialização
- **Lazy loading** para todos os relacionamentos
- Consultas com `JOIN FETCH` explícito para evitar `LazyInitializationException`

---

## API — Endpoints

### Autenticação (públicos)

| Método | Rota | Propósito | Request Body | Response |
|--------|------|-----------|-------------|----------|
| POST | `/auth/registro` | Criar novo usuário | Dados do usuário | Usuário criado (status 201) |
| POST | `/auth/login` | Autenticar e obter token | username + senha | `{ "token": "...", "nome": "..." }` |

### Tarefas (requerem autenticação)

| Método | Rota | Propósito |
|--------|------|-----------|
| POST | `/tarefas` | Criar nova tarefa |
| GET | `/tarefas` | Listar tarefas do usuário autenticado |
| GET | `/tarefas/{id}` | Obter tarefa por ID |
| PUT | `/tarefas/{id}` | Atualizar tarefa |
| PATCH | `/tarefas/{id}/concluir` | Marcar tarefa como concluída |
| DELETE | `/tarefas/{id}` | Remover tarefa |

### Usuário (requer autenticação)

| Método | Rota | Propósito |
|--------|------|-----------|
| GET | `/usuarios/me` | Dados do próprio usuário |

### Regras de autorização por tarefa

Ao acessar `/tarefas/{id}`:

| Cenário | Status code | Comportamento |
|---------|-------------|---------------|
| Tarefa **não existe** | `404` | Lançar `ResourceNotFoundException` |
| Tarefa **existe** e é **do usuário autenticado** | `200` | Operação normal |
| Tarefa **existe** mas **não é do usuário** (leitura: GET) | `404` | Mesmo status de "não existe" — não revelar existência |
| Tarefa **existe** mas **não é do usuário** (escrita: PUT, PATCH, DELETE) | `403` | Lançar `AccessDeniedException` |

---

## Segurança (JWT)

### Fluxo
1. Usuário faz POST `/auth/login` com username + senha
2. Servidor valida credenciais e retorna JWT + nome
3. Cliente envia JWT no header `Authorization: Bearer <token>` em todas as requisições autenticadas

### Payload do JWT
```json
{
  "sub": "<username>"
}
```
Apenas o username — sem roles, sem dados extras.

### Resposta de login
```json
{
  "token": "<jwt_string>",
  "nome": "<nome_do_usuario>"
}
```
Apenas token e nome — sem outros dados.

### Configuração do token
- Duração: **3 horas** (10.800 segundos)
- Sem refresh token por enquanto
- Rotas públicas: `/auth/registro`, `/auth/login`
- Rotas protegidas: todas as demais (incluindo `/tarefas/*` e `/usuarios/*`)

---

## Testes

### Configuração
- Banco H2 em memória para testes
- `@SpringBootTest` com perfil de teste para usar H2
- Dependência de teste: `spring-boot-starter-test`

### O que testar
- Operações CRUD de tarefas
- Regras de autorização (404 vs 403)
- Autenticação (registro, login, token inválido/expirado)
- Case-insensitivity de categorias
- Endpoint `/usuarios/me`
