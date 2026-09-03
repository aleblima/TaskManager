# ADR 013: Unicidade Normalizada de Categoria

**Status:** Accepted  
**Data:** 2026-09-02  

## Contexto

Categorias são reutilizadas sem distinguir maiúsculas de minúsculas, mas a
consulta case-insensitive do service não impede duas criações concorrentes nem
garante essa identidade no banco. Um índice funcional específico do PostgreSQL
reduziria a portabilidade exigida entre PostgreSQL e H2.

## Decisão

`Categoria` mantém `nome` para apresentação e passa a persistir
`nomeNormalizado`, calculado por `trim().toLowerCase(Locale.ROOT)`. A coluna
normalizada é não nula e única. O service consulta por ela antes de criar; se
uma colisão de unicidade ocorrer em requisições concorrentes, consulta a
categoria vencedora e a reutiliza.

## Consequências

* **Positivas:** A identidade case-insensitive é garantida por H2 e PostgreSQL,
  inclusive sob concorrência, sem SQL específico de banco.
* **Negativas:** A entidade armazena uma coluna técnica adicional e toda criação
  de categoria deve preservar a normalização no service.
