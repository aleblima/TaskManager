# TaskManager — Migração para API RESTful (Spring Boot)

## Arquitetura (geral)

Migração de aplicação local (JavaFX + SQLite) para API RESTful, stateless e multiusuário com Spring Boot.

### Pilha tecnológica
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
| tarefas | List\<Tarefa\> | `@OneToMany` para Tarefa |

#### `Usuario`
| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long (PK) | Gerado automaticamente |
| nome | String | Nome real do usuário |
| username | String | Identificador único para login |
| senha | String | Armazenar com BCrypt |
| tarefas | List\<Tarefa\> | `@OneToMany` para Tarefa (1-N) |

#### `Tarefa`
| Campo | Tipo | Observação |
|-------|------|------------|
| id | Long (PK) | Gerado automaticamente |
| ... | ... | Campos existentes da branch Main |
| usuario | Usuario | `@ManyToOne` — dono da tarefa |
| categoria | Categoria | `@ManyToOne` — gerenciada pela tarefa (sem CRUD separado de categorias) |

### Relacionamentos
- **Categoria 1 → N Tarefa**: uma categoria pode ter várias tarefas
- **Usuario 1 → N Tarefa**: cada tarefa pertence a exatamente um usuário

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
