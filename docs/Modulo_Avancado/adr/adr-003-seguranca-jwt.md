# ADR 003: Segurança com JSON Web Tokens (JWT)

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
Uma API stateless requer um mecanismo seguro, leve e autocontido para autenticar requisições subsequentes ao login.

## Decisão
Decidimos implementar a segurança baseada em JWT (JSON Web Tokens) usando a biblioteca JJWT (versão 0.12.6):
1.  **Geração:** O login bem-sucedido via POST `/auth/login` emite um JWT assinado pelo servidor.
2.  **Payload:** O payload conterá apenas o claim padrão `sub` contendo o `username` do usuário autenticado. Nenhuma informação extra ou role será inclusa para manter o token o mais enxuto possível.
3.  **Expiração:** O tempo de vida do token será fixado em **3 horas** (10.800 segundos).
4.  **Armazenamento:** Sem mecanismo de *refresh token* inicialmente.

## Consequências
*   **Positivas:** Autenticação totalmente stateless (o servidor não precisa consultar o banco de dados nem sessão para validar a assinatura do token), facilidade de integração com clients modernos.
*   **Negativas:** Tokens emitidos não podem ser invalidados de forma simples antes da expiração padrão (3 horas), a menos que se implemente uma lista negra (blacklist), o que está fora do escopo atual.
