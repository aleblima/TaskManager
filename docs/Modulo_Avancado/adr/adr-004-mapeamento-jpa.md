# ADR 004: Mapeamento JPA e Relacionamentos

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
O uso incorreto de frameworks ORM (JPA/Hibernate) frequentemente gera problemas graves como serializações cíclicas infinitas em JSON, queries ineficientes (N+1), e acoplamento excessivo de entidades.

## Decisão
Decidimos pelas seguintes restrições de persistência e mapeamento:
1.  **Lazy Loading estrito:** Todos os relacionamentos `@ManyToOne` e `@OneToOne` devem usar obrigatoriamente `FetchType.LAZY`.
2.  **Mapeamentos Unidirecionais:** Os relacionamentos `Tarefa -> Usuario` e `Tarefa -> Categoria` são estritamente unidirecionais. É proibida a adição de anotações `@OneToMany` nas classes `Usuario` ou `Categoria`.
3.  **Queries customizadas:** Listagens e consultas de dados serão feitas via métodos dedicados no repository utilizando `JOIN FETCH` explícito para carregar relacionamentos preguiçosos, eliminando riscos de `LazyInitializationException` fora do escopo transacional.

## Consequências
*   **Positivas:** Prevenção mecânica de queries duplicadas (N+1) e controle estrito sobre o grafo de objetos carregado na memória. Evita ciclos recursivos na serialização JSON.
*   **Negativas:** Exige maior disciplina de escrita de queries customizadas `@Query` no Repository em vez de depender inteiramente da navegação implícita de objetos do Hibernate.
