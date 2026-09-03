# ADR 006: Tratamento de Autorização por Tarefa (404 vs 403)

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
Em sistemas multiusuário, o vazamento de informações sobre a existência de recursos de outros usuários por meio de IDs sequenciais (ID Enumeration) é uma vulnerabilidade de segurança séria (Broken Object Level Authorization - BOLA).

## Decisão
Decidimos mapear as respostas de segurança de acordo com a intenção da operação:
1.  **Operações de Leitura (GET `/tarefas/{id}`):** Se um usuário autenticado tentar obter uma tarefa inexistente OU que pertence a outro usuário, o sistema deve retornar **HTTP 404 (Not Found)**. Isso oculta a existência do recurso, de forma que o atacante não consiga diferenciar se a tarefa não existe ou se pertence a outra pessoa.
2.  **Operações de Escrita (PUT, PATCH, DELETE `/tarefas/{id}`):** Se um usuário tentar alterar ou deletar uma tarefa pertencente a outro usuário, o sistema lançará `AccessDeniedException` que é traduzido em **HTTP 403 (Forbidden)**. Nesses casos, o sistema reconhece que o recurso existe, mas proíbe explicitamente a modificação.

## Consequências
*   **Positivas:** Proteção robusta contra vazamento de metadados e enumeração de recursos por usuários mal-intencionados na leitura, mantendo a semântica de autorização apropriada na escrita.
*   **Negativas:** O cliente de frontend receberá um erro 404 genérico ao ler uma tarefa de outro usuário, sem poder diferenciar se o ID é inválido ou se é apenas um problema de permissão (o que é o comportamento de segurança desejado).
