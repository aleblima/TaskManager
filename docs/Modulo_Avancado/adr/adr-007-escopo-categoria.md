# ADR 007: Escopo Isolado de Categoria

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
Categorias são metadados que organizam tarefas. Elas não possuem ciclo de vida independente neste momento (não há CRUD direto de categorias, nem listagem independente delas). Criar toda a estrutura do Spring para Categoria (Controller, Service, DTOs, etc.) resultaria em overengineering.

## Decisão
Decidimos que o escopo de `Categoria` será restrito:
1.  **Arquivos Limitados:** `Categoria` possuirá exclusivamente as classes `Categoria` (Entity) e `CategoriaRepository` (Repository). Não devem existir classes de DTO, Service ou Controller para Categoria.
2.  **Gerenciamento Interno:** A busca e criação automática de categorias (que devem ser criadas na primeira vez que são vinculadas a uma tarefa, de forma case-insensitive) serão feitas internamente por métodos privados no `TarefaService`.

## Consequências
*   **Positivas:** Redução extrema de boilerplate e de classes extras sem propósito real de negócio imediato. Foco estrito no domínio principal (Tarefas).
*   **Negativas:** Caso surja a necessidade futura de CRUD ou compartilhamento avançado de Categorias, a lógica terá de ser refatorada e desacoplada de `TarefaService` para um serviço próprio.
