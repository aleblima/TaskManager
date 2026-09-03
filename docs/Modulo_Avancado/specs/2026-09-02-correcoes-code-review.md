# Correções do Code Review Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corrigir os achados relevantes do CodeRabbit sem alterar os contratos de segurança já aprovados para a API JWT stateless.

**Architecture:** As correções HTTP permanecem nas bordas existentes (DTO, mapper e entry point). A identidade de `Categoria` passa a ter uma chave técnica persistida; `TarefaServiceImpl` continua sendo o único orquestrador de categoria e usa uma transação independente somente para a tentativa de inserção, para que uma colisão de unicidade não invalide a transação da tarefa que fará a releitura.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Data JPA, Bean Validation, Spring Security, H2 nos testes, PostgreSQL no desenvolvimento, Maven Wrapper, GitHub Actions.

**Spec:** `tarefa-categoria-design.md`, `repository-design.md`, `service-design.md`, `dto-design.md`, `controller-design.md`, `setup-desenvolvimento.md`, `ci-github-actions-design.md`, `../adr/adr-013-unicidade-normalizada-categoria.md`

## Global Constraints

- Cobertura por linha mínima de 90%; cada teste cobre um caminho de execução exclusivo e comportamento observável.
- `Categoria` não ganha DTO, Controller ou Service dedicado; é gerenciada internamente por `TarefaServiceImpl`.
- `TarefaResponseDTO` expõe de `Usuario` e `Categoria` somente `id` e `nome`.
- A API continua JWT stateless; CSRF permanece desabilitado intencionalmente.
- O banco de desenvolvimento só é publicado em `127.0.0.1:5433`; a aplicação usa variáveis `DB_*` e o contêiner usa `POSTGRES_*`.
- Não versionar `../../../qodana.yaml`, pois é arquivo local ainda não incluído pelo usuário.

---

### Task 1: Endurecer a configuração de CI e do PostgreSQL local

**Files:**
- Modify: `../../../.github/workflows/ci.yml`
- Modify: `../../../docker-compose.yml`
- Test: validação declarativa dos dois arquivos

**Interfaces:**
- Produces: todos os checkouts da CI usam `persist-credentials: false`; PostgreSQL escuta somente no loopback do host.

- [ ] **Step 1: Fazer as alterações declarativas mínimas**

```yaml
# Em cada passo actions/checkout@v6:
with:
  persist-credentials: false

# Em docker-compose.yml:
ports:
  - "127.0.0.1:${POSTGRES_PORT:-5433}:5432"
```

- [ ] **Step 2: Validar os contratos de configuração**

Run: `rtk python3 -c 'import yaml; yaml.safe_load(open(".github/workflows/ci.yml")); yaml.safe_load(open("docker-compose.yml"))'`

Expected: exit code 0.

Run: `rtk docker compose config`

Expected: a porta publicada do serviço `postgres` é `127.0.0.1:5433` por padrão, sem iniciar contêineres.

- [ ] **Step 3: Revisar o diff de configuração**

Run: `rtk git diff --check`

Expected: exit code 0.

### Task 2: Corrigir os contratos HTTP e de serialização

**Files:**
- Modify: `../../../src/main/java/com/example/taskmanager/dto/RegistroRequestDTO.java`
- Modify: `../../../src/main/java/com/example/taskmanager/dto/TarefaResponseDTO.java`
- Modify: `../../../src/main/java/com/example/taskmanager/mapper/TarefaMapper.java`
- Modify: `../../../src/main/java/com/example/taskmanager/security/AuthenticationProblemDetailEntryPoint.java`
- Modify: `../../../src/test/java/com/example/taskmanager/controller/AuthControllerTest.java`
- Create: `../../../src/test/java/com/example/taskmanager/mapper/TarefaMapperTest.java`
- Create: `../../../src/test/java/com/example/taskmanager/security/AuthenticationProblemDetailEntryPointTest.java`

**Interfaces:**
- Produces: `RegistroRequestDTO(String nome, String username, String senha)` rejeita username em branco; `TarefaResponseDTO` passa a ser `(Long id, String titulo, String descricao, Boolean concluida, LocalDateTime dataCriacao, Long usuarioId, String usuarioNome, Long categoriaId, String categoriaNome)`; respostas 401 usam UTF-8 explícito.

- [ ] **Step 1: Escrever os testes que falham**

```java
// AuthControllerTest: caminho exclusivo da validação @NotBlank em username
RegistroRequestDTO request = new RegistroRequestDTO("Ana", "        ", "senha123");
mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isBadRequest());

// TarefaMapperTest: o nome do usuário deve atravessar o mapper sem expor entidade
assertEquals("Ana", new TarefaMapper().toDTO(tarefa).usuarioNome());

// AuthenticationProblemDetailEntryPointTest
entryPoint.commence(request, response, exception);
assertEquals("UTF-8", response.getCharacterEncoding());
assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("Não autorizado"));
```

- [ ] **Step 2: Executar os testes e confirmar a falha inicial**

Run: `rtk proxy sh ./mvnw test -Dtest=AuthControllerTest,TarefaMapperTest,AuthenticationProblemDetailEntryPointTest`

Expected: falha por aceitar username só com espaços, ausência de `usuarioNome` e/ou codificação não definida.

- [ ] **Step 3: Implementar os contratos mínimos**

```java
// RegistroRequestDTO
@NotBlank(message = "Username é obrigatório")
@Size(min = 8, max = 15, message = "Username deve ter entre 8 e 15 caracteres")
String username

// TarefaMapper.toDTO(...), imediatamente após usuarioId
tarefa.getUsuario().getNome(),

// AuthenticationProblemDetailEntryPoint.commence(...), antes de getWriter()
response.setCharacterEncoding(StandardCharsets.UTF_8.name());
```

- [ ] **Step 4: Executar os testes do contrato HTTP**

Run: `rtk proxy sh ./mvnw test -Dtest=AuthControllerTest,TarefaMapperTest,AuthenticationProblemDetailEntryPointTest`

Expected: todos passam.

### Task 3: Tornar a identidade de categoria normalizada e segura sob concorrência

**Files:**
- Modify: `../../../src/main/java/com/example/taskmanager/entity/Categoria.java`
- Modify: `../../../src/main/java/com/example/taskmanager/repository/CategoriaRepository.java`
- Modify: `../../../src/main/java/com/example/taskmanager/service/impl/TarefaServiceImpl.java`
- Modify: `../../../src/test/java/com/example/taskmanager/service/impl/TarefaServiceImplTest.java`
- Create: `../../../src/test/java/com/example/taskmanager/repository/CategoriaRepositoryTest.java`

**Interfaces:**
- Consumes: `Categoria.nome` é o valor de apresentação; a entrada de categoria já é validada pelo DTO.
- Produces: `Categoria` tem `String nomeNormalizado`; `CategoriaRepository.findByNomeNormalizado(String nomeNormalizado)`; `TarefaServiceImpl.buscarOuCriarCategoria(String nome)` normaliza com `trim().toLowerCase(Locale.ROOT)` e devolve a categoria já existente ou a vencedora de uma corrida.

- [ ] **Step 1: Escrever os testes que falham**

```java
// TarefaServiceImplTest: caminho de categoria nova
when(categoriaRepository.findByNomeNormalizado("nova")).thenReturn(Optional.empty());
tarefaService.criar(new TarefaRequestDTO("Tarefa", "Descricao", " Nova "), "ana12345");
verify(categoriaRepository).findByNomeNormalizado("nova");
verify(categoriaRepository).saveAndFlush(argThat(categoria ->
    categoria.getNome().equals(" Nova ") && categoria.getNomeNormalizado().equals("nova")));

// TarefaServiceImplTest: caminho exclusivo de colisão concorrente
when(categoriaRepository.findByNomeNormalizado("nova"))
    .thenReturn(Optional.empty(), Optional.of(categoriaExistente));
when(categoriaRepository.saveAndFlush(any(Categoria.class)))
    .thenThrow(new DataIntegrityViolationException("uk_categoria_nome_normalizado"));
assertSame(categoriaExistente, categoriaUsadaNaTarefa);

// CategoriaRepositoryTest: a restrição física impede a mesma chave normalizada
categoriaRepository.saveAndFlush(new Categoria(null, "Trabalho", "trabalho"));
assertThrows(DataIntegrityViolationException.class, () ->
    categoriaRepository.saveAndFlush(new Categoria(null, "TRABALHO", "trabalho")));
```

- [ ] **Step 2: Executar os testes e confirmar a falha inicial**

Run: `rtk proxy sh ./mvnw test -Dtest=TarefaServiceImplTest,CategoriaRepositoryTest`

Expected: falha porque não existe a coluna/chave normalizada nem o novo método de repositório.

- [ ] **Step 3: Implementar a chave e a tentativa em transação isolada**

```java
// Categoria
@Column(nullable = false)
private String nome;

@Column(nullable = false, unique = true)
private String nomeNormalizado;

// TarefaServiceImpl: construir TransactionTemplate com PROPAGATION_REQUIRES_NEW
// a partir de PlatformTransactionManager; na ausência inicial, persistir via saveAndFlush.
// A exceção DataIntegrityViolationException sai da transação interna já revertida;
// então a transação da tarefa executa findByNomeNormalizado(chave) e usa a vencedora.
private String normalizarNomeCategoria(String nome) {
    return nome.trim().toLowerCase(Locale.ROOT);
}
```

- [ ] **Step 4: Executar os testes de unidade e de JPA**

Run: `rtk proxy sh ./mvnw test -Dtest=TarefaServiceImplTest,CategoriaRepositoryTest`

Expected: todos passam, incluindo a restrição H2 e a recuperação após a exceção simulada.

- [ ] **Step 5: Verificar o contrato de arquitetura**

Run: `rtk proxy sh ./mvnw test -Dtest=ArchitectureTest`

Expected: passa; nenhuma camada dedicada de Categoria foi criada.

### Task 4: Completar a evidência de segurança e verificar a esteira inteira

**Files:**
- Modify: `../../../src/test/java/com/example/taskmanager/security/JwtAuthenticationFilterTest.java`
- Modify: arquivos de especificação já atualizados nesta árvore de trabalho

**Interfaces:**
- Produces: o teste do token JWT válido verifica explicitamente que o username extraído do token é o valor usado na autenticação; documentação e implementação permanecem coerentes.

- [ ] **Step 1: Adicionar a verificação do argumento no caminho feliz do filtro**

```java
verify(jwtTokenProvider).getUsernameFromToken("token-valido");
```

- [ ] **Step 2: Executar o teste focalizado**

Run: `rtk proxy sh ./mvnw test -Dtest=JwtAuthenticationFilterTest`

Expected: todos passam.

- [ ] **Step 3: Executar a suíte e a cobertura completas**

Run: `rtk proxy sh ./mvnw test`

Expected: exit code 0, JaCoCo reporta mínimo de 90% de linhas e nenhum teste falha.

- [ ] **Step 4: Validar qualidade do diff e os arquivos versionáveis**

Run: `rtk git diff --check`

Expected: exit code 0.

Run: `rtk git status --short`

Expected: `../../../qodana.yaml` continua sem stage; somente fontes, testes, docs, workflow e Compose aprovados aparecem como mudanças do trabalho.

## Self-Review

- Cobertura da spec: a chave `nomeNormalizado`, a unicidade física, a recuperação de corrida, o DTO de usuário, a validação do registro, UTF-8, CI e porta loopback possuem tarefas explícitas.
- Contratos intencionalmente preservados: não há alteração na chave JWT de desenvolvimento nem reativação de CSRF.
- Consistência: `findByNomeNormalizado`, `nomeNormalizado` e `usuarioNome` são usados com a mesma grafia em todas as tarefas.
- Verificação de lacunas: a recuperação concorrente usa `REQUIRES_NEW` porque capturar uma violação de constraint na mesma transação JPA pode deixá-la marcada para rollback; isso evita um teste artificial que passe apenas em Mockito.
