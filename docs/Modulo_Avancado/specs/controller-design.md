# Design: Camada HTTP, Segurança JWT e Controllers da API TaskManager

**Data:** 2026-08-22

## Objetivo

Definir a camada HTTP completa da API TaskManager: rotas versionadas, controllers, autenticação JWT stateless, tratamento de erros e documentação OpenAPI. Esta spec consome os contratos já definidos para DTOs e services, registrando também as evoluções posteriores necessárias para o ciclo de vida das tarefas.

## Escopo

| Componente | Responsabilidade |
|------------|-----------------|
| `AuthController` | Registro e login públicos |
| `UsuarioController` | Consulta do usuário autenticado |
| `TarefaController` | CRUD, conclusão e reabertura das tarefas do usuário autenticado |
| `SecurityConfig` e filtro JWT | Validação stateless do Bearer JWT e autorização das rotas |
| `GlobalExceptionHandler` | Conversão de exceções em `ProblemDetail` |

`Categoria` continua sem controller, DTO ou rota própria, conforme R-ESC-01.

---

## 1. Versionamento e Rotas

Todas as rotas funcionais usam o prefixo `/api/v1`.

### 1.1 Autenticação pública

| Método | Rota | Request | Resposta de sucesso |
|--------|------|---------|---------------------|
| POST | `/api/v1/auth/registro` | `RegistroRequestDTO` com `@Valid` | `201 Created` com `UsuarioResponseDTO` |
| POST | `/api/v1/auth/login` | `LoginRequestDTO` com `@Valid` | `200 OK` com `LoginResponseDTO` |

### 1.2 Usuário autenticado

| Método | Rota | Resposta de sucesso |
|--------|------|---------------------|
| GET | `/api/v1/usuarios/me` | `200 OK` com `UsuarioResponseDTO` |

O controller obtém o `username` da autenticação presente no `SecurityContext` e o repassa a `UsuarioService`. Não aceita identificador de usuário em path, query string ou body.

### 1.3 Tarefas do usuário autenticado

| Método | Rota | Request | Resposta de sucesso |
|--------|------|---------|---------------------|
| POST | `/api/v1/tarefas` | `TarefaRequestDTO` com `@Valid` | `201 Created` com `TarefaResponseDTO` |
| GET | `/api/v1/tarefas` | — | `200 OK` com lista de `TarefaResponseDTO` |
| GET | `/api/v1/tarefas/{id}` | — | `200 OK` com `TarefaResponseDTO` |
| PUT | `/api/v1/tarefas/{id}` | `TarefaRequestDTO` com `@Valid` | `200 OK` com `TarefaResponseDTO` |
| PATCH | `/api/v1/tarefas/{id}/concluir` | sem body | `200 OK` com `TarefaResponseDTO` |
| PATCH | `/api/v1/tarefas/{id}/reabrir` | sem body | `200 OK` com `TarefaResponseDTO` |
| DELETE | `/api/v1/tarefas/{id}` | — | `204 No Content` |

Os controllers nunca recebem nem retornam entidades JPA. O `username` é lido do `SecurityContext` e repassado explicitamente a `TarefaService`; não há `usuarioId` em query string, path ou corpo de requisição.

### 1.4 Conclusão e reabertura

- `PATCH /concluir` é idempotente: sempre garante `concluida = true`.
- `PATCH /reabrir` é idempotente: sempre garante `concluida = false`.
- Ambos não aceitam request body e aplicam a mesma autorização de operações de escrita: 404 quando a tarefa não existe e 403 quando pertence a outro usuário.

---

## 2. Segurança HTTP

### 2.1 Política de acesso

São públicas apenas as rotas de registro e login, além da documentação em `/swagger-ui/index.html` e `/v3/api-docs`.

Todas as demais rotas exigem um header `Authorization` no formato `Bearer <JWT>`.

Não haverá configuração CORS nesta etapa. A política de origens será definida quando existir um cliente web e suas origens forem conhecidas.

### 2.2 JWT stateless

- A configuração de segurança deve ser stateless e não criar sessão HTTP.
- Um filtro executado antes dos controllers extrai o Bearer JWT, valida assinatura e expiração e obtém exclusivamente o `sub` como username.
- Em token válido, o filtro cria a autenticação no `SecurityContext` usando o username.
- Token ausente, malformado, expirado ou inválido retorna `401 Unauthorized` e não permite a execução do controller.
- `JwtTokenProvider` deve manter a geração atual e disponibilizar operações para validar token e extrair o username.

---

## 3. Erros e Validação

`GlobalExceptionHandler`, a entrada de autenticação e o handler de acesso negado devem produzir respostas RFC 9457 `ProblemDetail`.

| Situação | Status | Regra |
|----------|--------|-------|
| Bean Validation inválida | 400 | `ProblemDetail` contém a propriedade `errors`, mapeando nome de campo para mensagem |
| Credenciais inválidas, token ausente ou inválido | 401 | Não expõe detalhes internos de autenticação |
| Usuário autenticado sem permissão de escrita | 403 | Aplicável a PUT, PATCH e DELETE de tarefa alheia |
| Recurso inexistente ou GET de tarefa alheia | 404 | Oculta a existência de tarefa de outro usuário |
| Username já existente no registro | 409 | Conflito com identificador único |

`ProblemDetail` não deve expor stack trace, entidade JPA, senha, segredo JWT nem detalhes internos de persistência.

---

## 4. OpenAPI

A configuração detalhada, as anotações por endpoint, a matriz de respostas e o roteiro de teste estão definidos em [openapi-swagger-design.md](./openapi-swagger-design.md). Esse documento é normativo para a implementação desta seção.

- Springdoc deve documentar os endpoints HTTP e os DTOs de request/response.
- Swagger UI fica disponível publicamente em `/swagger-ui/index.html`.
- A especificação OpenAPI fica disponível publicamente em `/v3/api-docs`.
- A documentação deve indicar Bearer JWT como esquema de segurança das operações protegidas.

---

## 5. Testes de Verificação

Os testes devem ser claros, simples e diretos; cada um verifica um único critério de aceite e não deve ser alterado ou removido apenas para passar.

1. Registro e login são públicos; os demais endpoints exigem JWT. Requisições sem token ou com token inválido retornam 401.
2. Login válido retorna token; credenciais inválidas retornam 401.
3. Registro com username existente retorna 409.
4. Erros de Bean Validation retornam 400 com `ProblemDetail.errors`.
5. `/usuarios/me` devolve somente o usuário autenticado.
6. Cada rota de tarefa delega ao método correto do service e usa apenas DTOs no contrato HTTP.
7. GET de tarefa inexistente ou alheia retorna 404.
8. PUT, PATCH e DELETE de tarefa alheia retornam 403.
9. Concluir e reabrir são idempotentes e não aceitam request body; ambos retornam 403 para tarefa alheia.
10. DELETE retorna 204 sem corpo.
11. Swagger UI e OpenAPI são públicos; a documentação indica Bearer JWT nas rotas protegidas.
12. A cobertura de código da implementação de controllers e segurança deve ser, no mínimo, 90%.

## Fora do Escopo

- Frontend, cliente web e configuração CORS.
- Roles, permissões administrativas, refresh token, logout ou blacklist de JWT.
- Endpoint de Categoria, paginação, filtros e ordenação de tarefas.
- Alterações no modelo JPA, repositories, DTOs existentes ou regras de persistência, exceto a evolução explicitamente registrada em `service-design.md` para reabrir tarefas.
