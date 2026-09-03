# TaskManager — Guia de Desenvolvimento & Invariantes

Este arquivo é a fonte da verdade para o desenvolvimento do projeto. Ele orienta desenvolvedores humanos e agentes de IA sobre as regras que **NUNCA** devem ser quebradas.

---

## 1. Fluxo de Mudança (Spec-First SDD)

Este projeto usa **Specification-Driven Development (SDD)** com especificações vivas (*Living Specs*). 
Se em algum momento for necessário alterar o comportamento do sistema ou adicionar recursos:

1. **Atualize a Spec correspondente** (em `docs/Modulo_Avancado/specs/`) e/ou 
   este 
   arquivo `AGENTS.md` (se afetar alguma invariante).
2. **Atualize os testes de verificação** associados para refletir o novo contrato (TDD).
3. **Altere o código** para fazer os testes passarem.
4. **Valide manualmente** sem commitar automaticamente. O commit é de responsabilidade do usuário.

### Política de Testes

* Os testes devem ser claros, simples e diretos, verificando comportamento observável em vez de detalhes de implementação.
* Cada implementação deve manter cobertura de código mínima de **90%**.
* Cada teste deve verificar um único critério de aceite, com nome que expresse esse critério.
* Um teste não pode ser removido nem alterado apenas para fazê-lo passar. Alterações em testes são permitidas somente quando uma mudança aprovada na spec altera o contrato que ele verifica.
* **Caminho de código exclusivo:** todo teste deve exercitar um caminho de execução que nenhum outro teste já cobre (ex: uma condicional, um `@RequestBody` opcional, um ramo de erro específico). Antes de considerar um teste válido, deve ser possível responder "qual linha de código só este teste alcança?" — se a resposta for "nenhuma", o teste é redundante e deve ser sinalizado para remoção, nunca aceito por analogia com testes vizinhos.
* **Nome expressa intenção, não sintaxe:** o nome do teste deve comunicar o comportamento/critério de aceite validado (ex: `reabrir_tarefa_alheia_retorna_403`), não detalhes de como a requisição foi montada (ex: `_sem_body`, `_com_parametro`) quando isso não corresponde a um caminho de código real no Controller/Service.
* **Validação por quebra proposital:** ao criar ou auditar um teste, o critério de aceite é: alterar de propósito o comportamento que ele deveria proteger (ex: trocar um valor de retorno, um argumento, um status code) e confirmar que o teste falha. Um teste que não falha diante de nenhuma quebra plausível não está validando nada e deve ser revisto antes de ser aceito.

### Invariant Especial sobre ADRs:
> ⚠️ **Sugerir ADR:** Se durante a modificação de uma spec, o agente ou o desenvolvedor identificar que as alterações tomadas contêm decisões de arquitetura transversais ou definitivas de infraestrutura que façam sentido virar um ADR, o agente **deve sugerir explicitamente essa criação** ao usuário para que ele possa revisar e validar a criação de um novo ADR antes de escrevê-lo.

*Histórico de decisões arquiteturais estruturais fica registrado 
imutavelmente em `docs/Modulo_Avancado/adr/`.*

---

## 2. Invariantes do Sistema (Regras Estritas)

Toda regra aqui listada possui um ID e é garantida por testes automatizados (CI) ou verificada no Code Review.

### Camada de Persistência & Modelagem (JPA)
*   **R-JPA-01 (Automático - ArchUnit):** Todos os relacionamentos `@ManyToOne` devem ser obrigatoriamente **Lazy Loading** (`FetchType.LAZY`).
*   **R-JPA-02 (Automático - ArchUnit):** Relacionamentos `Tarefa→Usuario` e `Tarefa→Categoria` são unidirecionais. É proibido adicionar `@OneToMany` do lado inverso (em `Usuario` ou `Categoria`).
*   **R-JPA-03 (Revisão):** Consultas de listas são feitas via métodos de query explícitos no Repository (com `JOIN FETCH` para evitar `LazyInitializationException`), nunca navegando relações (ex: `usuario.getTarefas()` não existe).
*   **R-JPA-04 (Automático - Teste):** `ddl-auto=create-drop` ativo no banco principal e de testes.

### Camada de Apresentação & DTOs
*   **R-DTO-01 (Automático - ArchUnit):** Controllers nunca expõem ou retornam entidades JPA diretamente; sempre utilizam DTOs.
*   **R-DTO-02 (Automático - Teste):** `TarefaResponseDTO` expõe apenas os dados comuns e, para `Usuario` e `Categoria`, expõe apenas os seus `id` (Long) e `nome` (String), sem o objeto completo.
*   **R-DTO-03 (Automático - ArchUnit/Teste):** O `usuarioId` usado em qualquer filtro ou criação de tarefas é extraído do SecurityContext (JWT). Não é permitido rotas como `/tarefas?usuarioId=X` ou passagem de `usuarioId` em corpos de requisição.
*   **R-DTO-04 (Automático - ArchUnit):** Bean Validation (`@NotNull`, `@NotBlank`, etc.) é obrigatória em todos os DTOs de Request.

### Escopo das Entidades
*   **R-ESC-01 (Automático - ArchUnit):** `Categoria` possui apenas as camadas Entity e Repository. Sem DTO, Service ou Controller dedicados. É gerenciada internamente por `TarefaService`.
*   **R-ESC-02 (Automático - ArchUnit):** `Usuario` e `Tarefa` possuem conjunto completo de camadas (Entity, Repository, Service, Controller, DTOs).

### Qualidade dos Testes (Camada de Verificação)
*   **R-TEST-01 (Revisão):** Nenhum teste é aceito (gerado por IA ou não) sem que exista um caminho de código exclusivo identificado para ele. Dois testes que exercitam a mesma linha de execução, com a mesma combinação de resultado esperado, são redundantes — mantém-se apenas um.
*   **R-TEST-02 (Revisão):** Testes de Controller (`@WebMvcTest`/MockMvc) verificam exclusivamente o contrato HTTP (rota, binding de parâmetros, status code, serialização, autorização) com o Service mockado; a lógica de negócio (regras de transição de estado, cálculos, validações internas) é responsabilidade dos testes de Service e não deve ser reafirmada na camada de Controller.
*   **R-TEST-03 (Revisão):** Testes de caminho feliz (sucesso) que dependem de argumentos extraídos do contexto (`@PathVariable`, SecurityContext/JWT) devem incluir `verify()` explícito sobre esses argumentos. Testes de caminho de erro cujo `thenThrow()` já depende do casamento exato dos argumentos do stub dispensam `verify()` adicional.
*   **R-TEST-04 (Automático - Cobertura):** cobertura mínima de **90%** (conforme Política de Testes) é medida por linha de código exercitada, não por número de testes — testes redundantes não contam a favor da cobertura real.

### Segurança & Autorização
*   **R-SEC-01 (Automático - Teste):** GET `/tarefas/{id}` em tarefa inexistente OU de outro usuário retorna **HTTP 404** (ResourceNotFoundException), ocultando a existência do recurso.
*   **R-SEC-02 (Automático - Teste):** PUT, PATCH ou DELETE em tarefa de outro usuário lança **HTTP 403** (AccessDeniedException).
*   **R-SEC-03 (Automático - Teste):** Token JWT assinado com tempo de expiração de **3 horas** e contendo apenas o `sub` (username) no payload.
*   **R-SEC-04 (Automático - Teste):** Login com credenciais inválidas ou token ausente/inválido retorna **HTTP 401**.

---

## 3. Índice de Especificações & Decisões

### Especificações Vivas (Living Specs)
- `docs/Modulo_Avancado/specs/tarefa-categoria-design.md` — Design de Tarefa e 
  Categoria
- `docs/Modulo_Avancado/specs/dto-design.md` — Design de DTOs e validações
- `docs/Modulo_Avancado/specs/repository-design.md` — Design dos Repositories
- `docs/Modulo_Avancado/specs/service-design.md` — Design da camada de Services
- `docs/Modulo_Avancado/specs/controller-design.md` — Design da camada HTTP, segurança JWT e controllers
- `docs/Modulo_Avancado/specs/setup-desenvolvimento.md` — Setup de desenvolvimento e inicialização da API
- `docs/Modulo_Avancado/specs/ci-github-actions-design.md` — Esteira de integração contínua, segurança e ruleset

### Registro de Decisões Arquiteturais (ADRs)
- `docs/Modulo_Avancado/adr/adr-001-migracao-api-rest.md` — Escolha do paradigma REST
- `docs/Modulo_Avancado/adr/adr-002-stack-tecnologica.md` — Versão Spring Boot 4.1.0 e PostgreSQL/H2
- `docs/Modulo_Avancado/adr/adr-003-seguranca-jwt.md` — Mecanismo de autenticação e formato do Token
- `docs/Modulo_Avancado/adr/adr-004-mapeamento-jpa.md` — Mapeamentos e lazy-loading
- `docs/Modulo_Avancado/adr/adr-005-estrutura-retorno-dtos.md` — Uso estrito de DTOs
- `docs/Modulo_Avancado/adr/adr-006-autorizacao-tarefas.md` — Tratamento de erros 404 vs 403
- `docs/Modulo_Avancado/adr/adr-007-escopo-categoria.md` — Isolar Categoria sem controller próprio
- `docs/Modulo_Avancado/adr/adr-008-configuracao-jpa.md` — Uso de ddl-auto=create-drop
- `docs/Modulo_Avancado/adr/adr-009-mappers-manuais.md` — Mapeamento manual entre entidades e DTOs
- `docs/Modulo_Avancado/adr/adr-010-ambiente-desenvolvimento-docker-compose.md` — Ambiente de desenvolvimento com Docker Compose
- `docs/Modulo_Avancado/adr/adr-011-camada-http-e-versionamento-api.md` — Camada HTTP e versionamento da API
- `docs/Modulo_Avancado/adr/adr-012-integracao-continua-github-actions.md` — Adoção de CI com GitHub Actions
- `docs/Modulo_Avancado/adr/adr-013-unicidade-normalizada-categoria.md` — Unicidade case-insensitive de categorias
