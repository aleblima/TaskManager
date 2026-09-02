# Design: OpenAPI e Swagger UI da TaskManager API

**Data:** 2026-09-01  
**Status:** Aprovado para implementação  
**Spec relacionada:** [controller-design.md](./controller-design.md), seção 4

## Objetivo

Transformar a documentação OpenAPI já exposta pela dependência Springdoc em uma interface de integração utilizável. A documentação precisa permitir que uma pessoa obtenha um JWT por `POST /api/v1/auth/login`, cole o token em **Authorize** e execute as rotas protegidas na própria Swagger UI.

O escopo é exclusivamente documentação/configuração OpenAPI e seus testes de contrato. Não altera rotas HTTP, DTOs, regras de domínio, geração/validação do JWT, tratamento de exceções, CORS ou autorização efetiva.

## Decisões fechadas

| Tema | Decisão |
|---|---|
| Biblioteca | Manter `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2`, já declarada no `pom.xml`. Não adicionar outra biblioteca de OpenAPI. |
| Documento e UI | Manter os caminhos padrão do Springdoc: JSON em `/v3/api-docs` e UI em `/swagger-ui/index.html`. Ambos são públicos. |
| Identidade | `info.title = "TaskManager API"`, `info.version = "v1"` e uma descrição curta em português explicando que a API administra tarefas do usuário autenticado. Não inventar contato, licença ou URL de servidor. |
| Segurança | Declarar um único esquema chamado `bearerAuth`, do tipo HTTP, scheme `bearer` e `bearerFormat` `JWT`. Não criar API key, OAuth2, cookie ou esquema alternativo. |
| Aplicação da segurança | Não usar requisito de segurança global. Aplicar `bearerAuth` no nível de classe dos controllers `UsuarioController` e `TarefaController`; `AuthController` fica explicitamente sem requisito. Assim, as duas operações de autenticação aparecem públicas e todas as operações dos outros dois grupos exigem Bearer. |
| Fluxo na UI | O usuário copia manualmente o valor `token` retornado pelo login e o cola no diálogo **Authorize**. Não introduzir JavaScript, interceptor ou preenchimento automático do token. |
| Organização | Usar as tags `Autenticação`, `Usuários` e `Tarefas`. Elas são a linguagem pública da documentação; não usar nomes de classes como agrupamento principal. |
| Erros | Documentar somente os status aplicáveis a cada operação, usando `ProblemDetail` como schema para respostas de erro. Para validação, descrever que `errors` é uma propriedade adicional que mapeia campo para mensagem. |

Não é necessário ADR: a adoção de Springdoc, os caminhos públicos e o mecanismo JWT já estão estabelecidos. As decisões acima detalham o contrato previamente definido, sem criar uma escolha transversal nova.

## Estrutura-alvo

Criar `src/main/java/com/example/taskmanager/config/OpenApiConfig.java` com responsabilidade única: expor o bean `OpenAPI` que contém os metadados e `components.securitySchemes.bearerAuth`.

O bean deve ser equivalente a:

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI taskManagerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskManager API")
                        .version("v1")
                        .description("API para gerenciamento das tarefas do usuário autenticado."))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
```

Importar as classes de `io.swagger.v3.oas.models.*`; não usar anotações de modelos no lugar desse bean. O nome literal `bearerAuth` é a ligação entre a configuração e `@SecurityRequirement` dos controllers, portanto não pode divergir.

## Anotações nos controllers

As anotações devem descrever o contrato já implementado e não duplicar lógica. Elas não devem receber/retornar entidades JPA, aceitar `usuarioId`, alterar `@Valid`, alterar o acesso ao `SecurityContext` nem capturar exceções.

### `AuthController`

1. Adicionar `@Tag(name = "Autenticação", description = "Registro e obtenção do token JWT.")` à classe.
2. Para `registro`, adicionar `@Operation` com resumo equivalente a `Registra um novo usuário` e `@ApiResponses` para:
   - `201`: `UsuarioResponseDTO`;
   - `400`: `ProblemDetail` para corpo inválido;
   - `409`: `ProblemDetail` para `username` já existente.
3. Para `login`, adicionar `@Operation` com resumo equivalente a `Autentica e gera um token JWT` e `@ApiResponses` para:
   - `200`: `LoginResponseDTO`, cuja propriedade `token` é o JWT a copiar para **Authorize**;
   - `400`: `ProblemDetail` para corpo inválido;
   - `401`: `ProblemDetail` para credenciais inválidas.
4. Não adicionar `@SecurityRequirement` à classe nem aos seus métodos. A ausência é intencional e deve ser preservada.

### `UsuarioController`

1. Adicionar `@Tag(name = "Usuários", description = "Dados do usuário autenticado.")` e `@SecurityRequirement(name = "bearerAuth")` à classe.
2. Para `me`, usar `@Operation` com resumo equivalente a `Obtém o usuário autenticado` e respostas:
   - `200`: `UsuarioResponseDTO`;
   - `401`: `ProblemDetail` para JWT ausente, inválido ou expirado;
   - `404`: `ProblemDetail` quando o `sub` do token não identifica usuário existente.
3. Não documentar parâmetro de usuário: a identidade vem exclusivamente do `SecurityContext`, conforme R-DTO-03.

### `TarefaController`

1. Adicionar `@Tag(name = "Tarefas", description = "CRUD e transições das tarefas do usuário autenticado.")` e `@SecurityRequirement(name = "bearerAuth")` à classe.
2. Adicionar `@Operation` a cada método. O resumo deve expressar a intenção: criar, listar, buscar, atualizar, concluir, reabrir e excluir.
3. Em cada `@PathVariable Long id`, adicionar `@Parameter(description = "Identificador da tarefa", required = true)` ou descrição semanticamente equivalente.
4. A matriz obrigatória de respostas é:

| Operação | Sucesso | Erros `ProblemDetail` |
|---|---|---|
| `POST /api/v1/tarefas` | `201` com `TarefaResponseDTO` | `400`, `401`, `404` |
| `GET /api/v1/tarefas` | `200` com array de `TarefaResponseDTO` | `401`, `404` |
| `GET /api/v1/tarefas/{id}` | `200` com `TarefaResponseDTO` | `401`, `404` |
| `PUT /api/v1/tarefas/{id}` | `200` com `TarefaResponseDTO` | `400`, `401`, `403`, `404` |
| `PATCH /api/v1/tarefas/{id}/concluir` | `200` com `TarefaResponseDTO` | `401`, `403`, `404` |
| `PATCH /api/v1/tarefas/{id}/reabrir` | `200` com `TarefaResponseDTO` | `401`, `403`, `404` |
| `DELETE /api/v1/tarefas/{id}` | `204` sem corpo | `401`, `403`, `404` |

5. `concluir` e `reabrir` não possuem `@RequestBody`, exemplo de corpo ou parâmetro de corpo. A ausência de body é parte do contrato.
6. Documentar `403` somente para escritas de tarefa alheia. Para `GET /{id}`, documentar `404`, pois a existência de tarefa alheia é ocultada por R-SEC-01.

## Schema de erros

Usar `@Content(schema = @Schema(implementation = ProblemDetail.class))` nas respostas de erro. A descrição das respostas `400` deve informar que a propriedade extra `errors` tem formato conceitual `{"campo": "mensagem"}`. Não criar DTO de erro apenas para documentação; a resposta em execução já é RFC 9457 `ProblemDetail`.

As mensagens e títulos não devem ser fixados em anotações como se fossem parte estável da API. O contrato é o status e o schema `ProblemDetail`; as mensagens atuais são implementadas por `GlobalExceptionHandler` e pelos handlers de segurança.

## Plano de implementação test-first

O executor deve seguir estritamente esta ordem. As etapas de teste não devem alterar os testes existentes apenas para acomodar a implementação.

### 1. Criar o teste de contrato da documentação

Criar `src/test/java/com/example/taskmanager/config/OpenApiDocumentationTest.java` como teste de integração leve com o contexto Spring real:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {
    @Autowired MockMvc mockMvc;
}
```

Não desabilitar filtros (`addFilters = false` é proibido nesse arquivo), pois o caminho exclusivo testado é a liberação real pelo `SecurityConfig`. Não mockar controllers, services, `JwtAuthenticationFilter` ou `JwtTokenProvider`: o documento é produzido pela aplicação real e esse é o contrato que interessa.

Antes de escrever cada teste, registrar a quebra que ele deve detectar. Cada caso abaixo alcança um resultado observável diferente e, portanto, não é redundante:

| Nome do teste | Requisição e asserções mínimas | Quebra que ele detecta |
|---|---|---|
| `documentacao_openapi_publica_expoe_metadados_e_esquema_bearer` | `GET /v3/api-docs` sem `Authorization` retorna `200`; JSON contém `info.title = "TaskManager API"`, `info.version = "v1"`, `components.securitySchemes.bearerAuth.type = "http"`, `.scheme = "bearer"` e `.bearerFormat = "JWT"`. | Remover a regra pública, o bean OpenAPI, os metadados ou configurar incorretamente o esquema JWT. |
| `documentacao_openapi_separa_operacoes_publicas_das_protegidas` | No JSON de `/v3/api-docs`, `POST /api/v1/auth/login` e `POST /api/v1/auth/registro` não têm requisito de segurança; `GET /api/v1/usuarios/me` e ao menos uma operação em `/api/v1/tarefas` têm `security[0].bearerAuth`. | Aplicar Bearer globalmente, esquecer a proteção documentada ou marcar autenticação como protegida. |
| `documentacao_openapi_expoe_respostas_de_erro_do_contrato` | No JSON, verificar uma operação representativa de cada ramo: login contém `400` e `401`; busca de tarefa contém `404`, mas não `403`; atualização contém `400`, `401`, `403` e `404`; cada resposta de erro aponta para `ProblemDetail`. | Omitir status, expor `403` no GET que deve ocultar recurso, ou documentar um schema de erro diferente. |
| `swagger_ui_publica_fica_disponivel_sem_jwt` | `GET /swagger-ui/index.html` sem `Authorization` retorna `200` e content type HTML compatível. | Remover a permissão da UI ou alterar indevidamente o caminho. |

Para os JSONPaths de requisitos de segurança, prefira selecionar a operação pelo caminho e método, por exemplo `$.paths['/api/v1/tarefas'].get.security[0].bearerAuth`. Não confirme apenas a presença textual de `bearerAuth` em qualquer lugar do documento: isso não prova sua associação às operações corretas.

### 2. Executar RED

Executar somente a nova classe antes da configuração/anotações:

```bash
./mvnw test -Dtest=OpenApiDocumentationTest
```

O resultado esperado é falha causada pela ausência de metadados, segurança por operação ou respostas explicitamente anotadas. Se o teste falhar por falta de PostgreSQL, garantir que o perfil `test` use `application-test.properties` e H2 antes de continuar; não substituir o teste de integração por `@WebMvcTest`, pois isso perderia a autorização real das rotas de documentação.

### 3. Implementar o mínimo para GREEN

1. Criar `OpenApiConfig` exatamente como definido nesta spec.
2. Adicionar as anotações de tags, segurança, operações, parâmetros e respostas aos três controllers, sem mudar as assinaturas ou seus corpos.
3. Não alterar `SecurityConfig`, pois ela já libera `/swagger-ui/**` e `/v3/api-docs/**`; qualquer alteração só é aceitável se o teste real demonstrar que uma URL exigida não está pública.
4. Reexecutar a classe de teste após cada pequena alteração até todos os quatro comportamentos passarem.

### 4. Refatorar e validar

1. Extrair somente constantes de descrição repetidas se isso deixar as anotações mais legíveis; não criar uma camada de documentação, DTOs ou controllers adicionais.
2. Executar os testes de controllers existentes para garantir que as anotações não afetaram binding, serialização ou autorização:

```bash
./mvnw test -Dtest=AuthControllerTest,TarefaControllerTest,UsuarioControllerTest,OpenApiDocumentationTest
```

3. Executar toda a suíte:

```bash
./mvnw test
```

4. Executar a ferramenta de cobertura configurada no projeto e confirmar mínimo de 90% por linha. Se não existir plugin de cobertura no `pom.xml`, registrar essa lacuna ao usuário; não alegar conformidade sem medição.
5. Fazer a validação manual: iniciar a aplicação, abrir `/swagger-ui/index.html`, registrar usuário, executar login, copiar apenas o valor de `token`, autorizar como Bearer, criar/listar uma tarefa e confirmar que a requisição protegida inclui `Authorization: Bearer <token>`.

## Critérios de aceite verificáveis

1. `/v3/api-docs` e `/swagger-ui/index.html` retornam `200` sem JWT quando os filtros de segurança estão ativos.
2. O documento declara `TaskManager API`, versão `v1`, descrição em português e exatamente o esquema HTTP Bearer `bearerAuth` com formato `JWT`.
3. Login e registro são documentados sem requisito de segurança; as operações de usuário e tarefa são documentadas com `bearerAuth`.
4. A UI agrupa as operações nas tags `Autenticação`, `Usuários` e `Tarefas`.
5. Cada rota documenta apenas seus status de sucesso e erro aplicáveis, com `ProblemDetail` nos erros; os PATCHes de estado não aceitam body e DELETE documenta `204` sem conteúdo.
6. A pessoa consegue obter o JWT pelo login e usá-lo manualmente via **Authorize** para chamar uma rota protegida.
7. Os quatro testes de documentação acima percorrem caminhos exclusivos, são observáveis na saída HTTP e falham sob a quebra descrita para cada um.
