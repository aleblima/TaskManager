# Design: Repositories para a API TaskManager

**Data:** 2026-07-25

## Objetivo

Definir as interfaces de Repository (Spring Data JPA) para cada entidade do
projeto, incluindo queries customizadas e métodos derivados necessários para
os endpoints da API.

## Escopo

| Entidade | Repository | Escopo completo |
|----------|-----------|----------------|
| Categoria | `CategoriaRepository` | Entity + Repository (sem Service/Controller/DTO) |
| Usuario | `UsuarioRepository` | Entity + Repository + Service + Controller + DTOs |
| Tarefa | `TarefaRepository` | Entity + Repository + Service + Controller + DTOs |

---

## Repositories

### CategoriaRepository

Responsável por buscas e criação de categorias internamente pelo `TarefaService`.
Categorias são **case-insensitive** ("trabalho" = "Trabalho").

| Método | Tipo de retorno | Descrição |
|--------|----------------|-----------|
| `findByNomeIgnoreCase(String nome)` | `Optional<Categoria>` | Busca categoria por nome, ignorando caixa |

**Extends:** `JpaRepository<Categoria, Long>`

**Justificativa para método derivado:** O Spring Data gera automaticamente
a query `WHERE LOWER(c.nome) = LOWER(:nome)`, suportado tanto pelo H2
(testes) quanto pelo PostgreSQL (produção). Sem necessidade de `@Query`
customizado.

---

### UsuarioRepository

Responsável por buscas de usuários para autenticação (login) e perfil (`/usuarios/me`).

| Método | Tipo de retorno | Descrição |
|--------|----------------|-----------|
| `findByUsername(String username)` | `Optional<Usuario>` | Busca usuário por username (login + JWT) |

**Extends:** `JpaRepository<Usuario, Long>`

**Observação:** `findById(Long id)` já é fornecido pelo `JpaRepository` e
resolve o endpoint `GET /usuarios/me`. Não há necessidade de query customizada
para esse caso.

---

### TarefaRepository

Responsável por consultas de tarefas com carregamento eager dos relacionamentos
(`usuario` e `categoria`), evitando `LazyInitializationException`.

#### Query de listagem (GET `/tarefas`)

| Método | Tipo de retorno | Descrição |
|--------|----------------|-----------|
| `findByUsuarioId(Long usuarioId)` | `List<Tarefa>` | Lista todas as tarefas do usuário com relacionamentos carregados |

```java
@Query("SELECT t FROM Tarefa t " +
       "JOIN FETCH t.usuario " +
       "JOIN FETCH t.categoria " +
       "WHERE t.usuario.id = :usuarioId")
List<Tarefa> findByUsuarioId(@Param("usuarioId") Long usuarioId);
```

#### Query por ID (GET `/tarefas/{id}`, PUT, PATCH, DELETE)

| Método | Tipo de retorno | Descrição |
|--------|----------------|-----------|
| `findByIdComRelacionamentos(Long id)` | `Optional<Tarefa>` | Busca tarefa por ID com relacionamentos carregados |

```java
@Query("SELECT t FROM Tarefa t " +
       "JOIN FETCH t.usuario " +
       "JOIN FETCH t.categoria " +
       "WHERE t.id = :id")
Optional<Tarefa> findByIdComRelacionamentos(@Param("id") Long id);
```

**Extends:** `JpaRepository<Tarefa, Long>`

**Por que `@Query` explícito e não `EntityGraph`:**
- `@Query` é mais previsível — o SQL gerado é visível e explícito
- `EntityGraph` pode ter comportamento inesperado com múltiplos níveis
- `JOIN FETCH` é mais transparente e não depende do contexto de transação

**Por que não `JpaSpecificationExecutor`:**
- O AGENTS.md não define filtros dinâmicos (por título, status, etc.)
- Se necessário no futuro, é trivial adicionar a extensão

---

## Decisões de Implementação

### Operações de escrita (save, delete)

| Operação | Abordagem | Justificativa |
|----------|-----------|---------------|
| Criar tarefa | `save(tarefa)` do JpaRepository | Operação padrão |
| Atualizar tarefa | `save(tarefa)` do JpaRepository | Service já carrega entidade via `findByIdComRelacionamentos` |
| Toggle conclusão | `save(tarefa)` do JpaRepository | Service inverte `concluida` na entidade carregada |
| Deletar tarefa | `deleteById(id)` do JpaRepository | Verificação de ownership já feita no service layer |

### Lazy loading e JOIN FETCH

- Todos os relacionamentos (`Tarefa→Usuario`, `Tarefa→Categoria`) são `FetchType.LAZY`
- Queries de leitura usam `JOIN FETCH` explícito para carregar relacionamentos
- Operações de escrita não precisam de `JOIN FETCH` — o service já tem a entidade carregada
- `spring.jpa.open-in-view=false`; testes de repository devem verificar as relações inicializadas fora de transação aberta.

### Case-insensitivity de categorias

- Implementada via método derivado `findByNomeIgnoreCase` no `CategoriaRepository`
- O Spring Data gera `WHERE LOWER(nome) = LOWER(:nome)` automaticamente
- Funciona tanto no H2 (testes) quanto no PostgreSQL (produção)

---

## Arquivos a Criar

1. `src/main/java/com/example/taskmanager/repository/CategoriaRepository.java`
2. `src/main/java/com/example/taskmanager/repository/UsuarioRepository.java`
3. `src/main/java/com/example/taskmanager/repository/TarefaRepository.java`

## Fora do Escopo

- Services, Controllers, Exception Handling — definidos em specs separadas
- Testes dos repositories — definidos em spec separada
- Configuração de transações (`@Transactional`) — definida no service layer
