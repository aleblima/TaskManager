# ADR 009: Mapeamento Manual entre Entidades e DTOs

**Status:** Accepted  
**Data:** 2026-08-21  

## Contexto

A aplicação utiliza DTOs para isolar a API das entidades JPA. Para converter entre esses tipos, seria possível adotar uma biblioteca de mapeamento automático, como o ModelMapper. Porém, a conveniência dessa abordagem reduz a visibilidade sobre quais campos são lidos, transformados ou expostos, especialmente em respostas que devem omitir dados internos e em associações JPA com carregamento preguiçoso.

## Decisão

Decidimos realizar os mapeamentos entre entidades e DTOs de forma manual, em métodos ou classes de mapper explícitos, sem utilizar o ModelMapper.

Os mappers devem declarar campo a campo os dados copiados e preservar as regras de apresentação já estabelecidas. Em particular, o mapeamento de `Tarefa` para `TarefaResponseDTO` deve expor de `Usuario` e `Categoria` somente `id` e `nome`, sem retornar as entidades ou seus grafos de relacionamento.

## Consequências

* **Positivas:** Os contratos de resposta permanecem explícitos e fáceis de revisar; reduz-se o risco de expor campos sensíveis ou internos por configuração implícita; o comportamento diante de relações lazy é previsível; e os mapeamentos são simples de depurar e testar sem reflexão em tempo de execução.
* **Negativas:** Há mais código repetitivo e cada novo campo exige alteração deliberada no mapper correspondente. Em troca, essa alteração torna a evolução do contrato visível na revisão e nos testes.
