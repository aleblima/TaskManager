# Design: Camada de Services, Mappers e Exceções para a API TaskManager

**Data:** 2026-07-28

## Objetivo

Definir as interfaces, implementações, mappers e tratamento de exceções para a camada de serviço da API `TaskManager`, seguindo os princípios SOLID, orientação a interfaces, e os requisitos especificados no `AGENTS.md`.

## Escopo

| Componente | Classes / Interfaces | Responsabilidade |
|------------|---------------------|------------------|
| **Services** | `AuthService`, `UsuarioService`, `TarefaService` | Contratos da regra de negócio |
| **Implementações** | `AuthServiceImpl`, `UsuarioServiceImpl`, `TarefaServiceImpl` | Lógica de negócio, transacionalidade e autorização |
| **Mappers** | `UsuarioMapper`, `TarefaMapper` | Conversão desacoplada entre DTOs e Entidades |
| **Exceções** | `ResourceNotFoundException`, `AccessDeniedException`, `RegraDeNegocioException` | Exceções de domínio capturadas centralizadamente |

---

## 1. Estrutura de Pacotes

```text
com.example.taskmanager/
├── service/
│   ├── AuthService.java            (Interface)
│   ├── UsuarioService.java          (Interface)
│   ├── TarefaService.java           (Interface)
│   └── impl/
│       ├── AuthServiceImpl.java    (Implementação)
│       ├── UsuarioServiceImpl.java (Implementação)
│       └── TarefaServiceImpl.java  (Implementação)
├── mapper/
│   ├── UsuarioMapper.java          (Componente @Component)
│   └── TarefaMapper.java           (Componente @Component)
└── exception/
    ├── ResourceNotFoundException.java
    ├── AccessDeniedException.java
    └── RegraDeNegocioException.java
```

---

## 2. Exceções de Domínio

- **`ResourceNotFoundException`**: Lançada quando um recurso (tarefa, usuário) não é encontrado no banco de dados. Mapeada para **HTTP 404** no `@ControllerAdvice`.
- **`AccessDeniedException`**: Lançada em operações de escrita (PUT, PATCH, DELETE) em tarefas pertencentes a outro usuário. Mapeada para **HTTP 403** no `@ControllerAdvice`.
- **`RegraDeNegocioException`**: Lançada para conflitos de regra de negócio,
  como tentativa de registrar username já existente. Mapeada para **HTTP 409**.
- **`CredenciaisInvalidasException`**: Lançada para credenciais inválidas no
  login. Mapeada para **HTTP 401**.

---

## 3. Interfaces e Contratos dos Serviços

### 3.1 `AuthService`

Lida exclusivamente com o fluxo de autenticação e registro.

```java
public interface AuthService {
    UsuarioResponseDTO registrar(RegistroRequestDTO registroRequest);
    LoginResponseDTO login(LoginRequestDTO loginRequest);
}
```

### 3.2 `UsuarioService`

Responsável pelo gerenciamento de dados do usuário autenticado.

```java
public interface UsuarioService {
    UsuarioResponseDTO obterUsuarioAtual(String usernameAutenticado);
}
```

### 3.3 `TarefaService`

Responsável pelas operações de CRUD e ciclo de vida de tarefas, recebendo o `username` autenticado explicitamente.

```java
public interface TarefaService {
    TarefaResponseDTO criar(TarefaRequestDTO tarefaRequest, String usernameAutenticado);
    List<TarefaResponseDTO> listarTodas(String usernameAutenticado);
    TarefaResponseDTO buscarPorId(Long id, String usernameAutenticado);
    TarefaResponseDTO atualizar(Long id, TarefaRequestDTO tarefaRequest, String usernameAutenticado);
    TarefaResponseDTO marcarComoConcluida(Long id, String usernameAutenticado);
    void deletar(Long id, String usernameAutenticado);
}
```

### Convenção de Nomenclatura de Parâmetros

- **DTOs de Request** (sufixo `Request`): parâmetros como `registroRequest`, `loginRequest`, `tarefaRequest` — vêm do **corpo da requisição HTTP**, recebidos pelo Controller e repassados ao Service.
- **DTOs de Response** (sufixo `Response`): nunca são parâmetros de entrada — são apenas o **retorno** dos métodos do Service.
- **`usernameAutenticado`**: extraído do token JWT (`SecurityContext`) pelo Controller e repassado ao Service. **Nunca** vem do corpo da requisição nem de query params.

---

## 4. Mappers Dedicados

Classes anotadas com `@Component` para isolar a conversão entre DTOs e Entidades, garantindo SRP (Single Responsibility Principle):

### 4.1 `UsuarioMapper`
- `toEntity(RegistroRequestDTO dto) -> Usuario`
- `toDTO(Usuario entity) -> UsuarioResponseDTO`

### 4.2 `TarefaMapper`
- `toEntity(TarefaRequestDTO dto, Usuario usuario, Categoria categoria) -> Tarefa`
- `toDTO(Tarefa entity) -> TarefaResponseDTO`
  - *Regra:* Expõe em `Usuario` e `Categoria` apenas os seus IDs (`Long`) e nomes (`String`), nunca os objetos JPA completos.

---

## 5. Regras de Implementação (`ServiceImpl`)

### 5.1 `AuthServiceImpl`
- **Anotação**: `@Service`, `@Transactional`.
- **Dependências**: `UsuarioRepository`, `PasswordEncoder`, `JwtTokenProvider` (ou serviço JWT), `UsuarioMapper`.
- **`registrar`**:
  - Verifica se o username já existe (`UsuarioRepository.findByUsername`). Se existir, lança `RegraDeNegocioException`.
  - Encripta a senha com BCrypt (`PasswordEncoder`).
  - Salva a nova entidade `Usuario` e retorna `UsuarioResponseDTO`.
- **`login`**:
  - Valida credenciais e gera token JWT.
  - Retorna `LoginResponseDTO` com o token JWT e o nome do usuário.

### 5.2 `UsuarioServiceImpl`
- **Anotação**: `@Service`, `@Transactional(readOnly = true)`.
- **Dependências**: `UsuarioRepository`, `UsuarioMapper`.
- **`obterUsuarioAtual(username)`**:
  - Busca usuário pelo `username`. Se não encontrado, lança `ResourceNotFoundException`.
  - Retorna `UsuarioResponseDTO`.

### 5.3 `TarefaServiceImpl`
- **Anotação**: `@Service`, `@Transactional(readOnly = true)` na classe. Métodos de escrita (`criar`, `atualizar`, `marcarComoConcluida`, `deletar`) anotados com `@Transactional`.
- **Dependências**: `TarefaRepository`, `CategoriaRepository`, `UsuarioRepository`, `TarefaMapper`.
- **Gerenciamento de Categoria**:
  - Método auxiliar `buscarOuCriarCategoria(String nome)`:
    - Normaliza o nome com `trim().toLowerCase(Locale.ROOT)` e consulta
      `CategoriaRepository.findByNomeNormalizado(nomeNormalizado)`.
    - Se encontrar, reutiliza a categoria existente.
    - Se não encontrar, cria a categoria com `nome` preservado e
      `nomeNormalizado` preenchido.
    - Se a criação concorrer com outra requisição e violar a unicidade, consulta
      novamente pela chave normalizada e reutiliza a categoria vencedora.
- **Autorização e Segurança por Tarefa**:
  - **Criar (`criar`)**:
    - Busca o `Usuario` pelo `username`. Se não existir, lança `ResourceNotFoundException`.
    - Busca/cria a `Categoria` informada no DTO.
    - Converte DTO para Entidade, define `concluida = false`, auto-preenche `dataCriacao` (`LocalDateTime.now()`) e salva a tarefa.
  - **Listar (`listarTodas`)**:
    - Busca usuário pelo `username`. Lança `ResourceNotFoundException` se não encontrado.
    - Chama `TarefaRepository.findByUsuarioId(usuario.getId())` com `JOIN FETCH`.
    - Mapeia para `List<TarefaResponseDTO>`.
  - **Buscar Por ID (`buscarPorId`)**:
    - Chama `TarefaRepository.findByIdComRelacionamentos(id)`.
    - **Regra de Segurança (GET)**: Se a tarefa não existir **OU** não pertencer ao usuário autenticado (`!tarefa.getUsuario().getUsername().equals(username)`), lança `ResourceNotFoundException` (HTTP 404), omitindo sua existência para outros usuários.
  - **Atualizar (`atualizar`)**:
    - Busca a tarefa por ID com relacionamentos.
    - Se a tarefa não existir, lança `ResourceNotFoundException` (HTTP 404).
    - **Regra de Segurança (PUT)**: Se a tarefa pertencer a outro usuário, lança `AccessDeniedException` (HTTP 403).
    - Atualiza título, descrição e categoria (busca/cria nova categoria se o nome mudou) e salva.
  - **Marcar Como Concluída (`marcarComoConcluida`)**:
    - Busca a tarefa por ID com relacionamentos.
    - Se não existir, lança `ResourceNotFoundException` (HTTP 404).
    - **Regra de Segurança (PATCH)**: Se a tarefa pertencer a outro usuário, lança `AccessDeniedException` (HTTP 403).
    - Altera `concluida` para `true` (ou alterna status) e salva.
  - **Deletar (`deletar`)**:
    - Busca a tarefa por ID.
    - Se não existir, lança `ResourceNotFoundException` (HTTP 404).
    - **Regra de Segurança (DELETE)**: Se a tarefa pertencer a outro usuário, lança `AccessDeniedException` (HTTP 403).
    - Remove a tarefa via `TarefaRepository.delete(tarefa)`.

---

## Decisões Arquiteturais e SOLID

1. **SRP (Single Responsibility Principle)**: Mappers isolam conversões DTO/Entidade; Services contêm apenas lógica de negócio e regras de autorização.
2. **DIP (Dependency Inversion Principle)**: Controllers dependem das interfaces dos serviços (`AuthService`, `UsuarioService`, `TarefaService`), facilitando testes com mocks.
3. **Desacoplamento do Spring Security**: O `username` é repassado explicitamente da Controller para a Service, sem dependência estática do `SecurityContextHolder` no Service layer.
4. **Isolamento de Categorias**: `Categoria` não possui Controller/Service expostos, sendo totalmente gerida via `TarefaServiceImpl`.

---

## Arquivos a Criar

1. `src/main/java/com/example/taskmanager/exception/ResourceNotFoundException.java`
2. `src/main/java/com/example/taskmanager/exception/AccessDeniedException.java`
3. `src/main/java/com/example/taskmanager/exception/RegraDeNegocioException.java`
4. `src/main/java/com/example/taskmanager/mapper/UsuarioMapper.java`
5. `src/main/java/com/example/taskmanager/mapper/TarefaMapper.java`
6. `src/main/java/com/example/taskmanager/service/AuthService.java`
7. `src/main/java/com/example/taskmanager/service/UsuarioService.java`
8. `src/main/java/com/example/taskmanager/service/TarefaService.java`
9. `src/main/java/com/example/taskmanager/service/impl/AuthServiceImpl.java`
10. `src/main/java/com/example/taskmanager/service/impl/UsuarioServiceImpl.java`
11. `src/main/java/com/example/taskmanager/service/impl/TarefaServiceImpl.java`

---

## Atualização de Decisões (2026-08-22)

Esta seção registra evoluções posteriores sem remover as decisões que orientaram a implementação inicial.

- **Username duplicado:** o registro de um username já existente deve resultar em `409 Conflict` na camada HTTP. O código de `401 Unauthorized` permanece reservado para credenciais inválidas, token ausente ou token inválido.
- **Reabertura de tarefa:** `TarefaService` passa a expor `reabrir(Long id, String usernameAutenticado) -> TarefaResponseDTO`. A operação aplica as mesmas regras de autorização de escrita de `marcarComoConcluida` e garante `concluida = false` de forma idempotente.
