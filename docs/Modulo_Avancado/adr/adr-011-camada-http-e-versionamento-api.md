# ADR 011: Camada HTTP e Versionamento da API

**Status:** Accepted  
**Data:** 2026-08-22  

## Contexto

As regras de domínio, DTOs, persistência e JWT já estão definidas, mas a API ainda não possui uma camada HTTP que exponha esses contratos de forma segura e versionada. A aplicação precisa ser consumida por clientes HTTP sem depender de frontend próprio, preservando o isolamento entre entidades JPA e a API pública.

## Decisão

1. As rotas funcionais serão expostas sob o prefixo `/api/v1`.
2. A autenticação será stateless por Bearer JWT. Um filtro valida o token, obtém o `sub` como username e popula o `SecurityContext` antes dos controllers.
3. Apenas registro, login e documentação OpenAPI serão públicos. Todas as demais rotas exigirão JWT válido.
4. Controllers dependem exclusivamente das interfaces de service, recebem e retornam DTOs e extraem o username do `SecurityContext`.
5. Erros HTTP usarão `ProblemDetail`; falhas de validação incluirão uma propriedade `errors` por campo.
6. O registro de username já existente retorna `409 Conflict`. Esta decisão complementa a spec de services sem alterar as ADRs anteriores, que não definem esse status.
7. O ciclo de vida de tarefa terá ações idempotentes de concluir e reabrir, ambas protegidas pela autorização de escrita já estabelecida na ADR-006.

## Consequências

* **Positivas:** A API tem contrato público versionado, seguro e explorável via Swagger UI sem frontend; a identidade autenticada chega aos services sem depender de dados fornecidos pelo cliente; e os erros ficam previsíveis para consumidores HTTP.
* **Negativas:** A camada passa a exigir configuração de segurança, filtro JWT, handlers de erro e testes de integração adicionais. Clientes futuros deverão chamar as rotas com prefixo `/api/v1` e enviar Bearer JWT nas operações protegidas.
