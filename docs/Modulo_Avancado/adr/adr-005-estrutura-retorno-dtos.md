# ADR 005: Estrutura de Retorno e Uso Estrito de DTOs

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
Expor entidades JPA diretamente em controllers acopla o modelo de banco de dados à API pública, podendo causar vazamento de dados sensíveis (como senhas), além de quebras repentinas na API caso a tabela sofra modificações estruturais.

## Decisão
Decidimos pelas seguintes restrições na camada de apresentação:
1.  **Isolamento total:** Controllers REST nunca devem receber nem retornar entidades JPA. Toda entrada e saída será mapeada em classes DTO (Data Transfer Objects).
2.  **Referenciação Enxuta:** `TarefaResponseDTO` nunca exporá o objeto completo de `Usuario` ou `Categoria`. Em vez disso, exporá apenas o `id` (Long) e o `nome` (String) de cada um de forma desnormalizada para fins de conveniência do cliente.
3.  **Validação Obrigatória:** Bean Validation (`@NotBlank`, `@NotNull`, etc.) é obrigatória em todos os DTOs de Request de entrada, validada automaticamente via `@Valid` na controller.

## Consequências
*   **Positivas:** Desacoplamento arquitetural, maior segurança (impossível vazar campos internos como hash de senhas acidentalmente), contratos de API estáveis e fáceis de validar estruturalmente.
*   **Negativas:** Aumento no número de classes auxiliares no projeto (necessidade de mappers dedicados e múltiplas classes DTO).
