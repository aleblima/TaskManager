# ADR 012: Integração Contínua com GitHub Actions

**Status:** Accepted  
**Data:** 2026-09-02  

## Contexto

O projeto possui testes automatizados, ArchUnit e uma regra JaCoCo de cobertura
mínima, mas essas garantias não eram verificadas por um sistema independente
antes do merge. Como `main` só aceita alterações por Pull Request, a validação
precisa ser acionada nesse ponto e integrada ao ruleset do GitHub.

Também é necessário reduzir o risco de introduzir dependências vulneráveis e
ter visibilidade de possíveis problemas de segurança no código Java, sem criar
CD ou um ambiente de produção inexistente.

## Decisão

Adotamos GitHub Actions como CI do repositório, exclusivamente para Pull
Requests destinados a `main`.

1. O job `test` executará `sh ./mvnw test` com Java 21 e será obrigatório para
   merge. Ele preserva o Maven como fonte de verdade para testes, ArchUnit e
   JaCoCo.
2. O job `dependency-review` revisará dependências introduzidas no Pull Request
   e será obrigatório para merge, após a confirmação de disponibilidade do
   Dependency graph no repositório.
3. CodeQL analisará o código Java e publicará seus achados como informação de
   segurança. Ele não bloqueará merges até que os achados iniciais tenham sido
   triados e uma política específica seja aprovada.
4. Relatórios JaCoCo serão publicados como artefatos de diagnóstico; não são
   um mecanismo adicional de decisão de cobertura.
5. O workflow usará permissões mínimas, não receberá segredos e não executará
   deploy, Docker ou banco PostgreSQL.

## Consequências

* **Positivas:** Regressões e cobertura insuficiente passam a ser bloqueadas
  antes do merge; dependências vulneráveis têm uma barreira preventiva; e
  alertas CodeQL ficam centralizados no GitHub sem interromper inicialmente o
  fluxo de desenvolvimento.
* **Negativas:** Pull Requests passam a depender da disponibilidade dos runners
  e serviços GitHub; as execuções aumentam o tempo até merge; Dependency review
  exige configuração e disponibilidade do Dependency graph; e alertas CodeQL
  exigirão triagem periódica para manterem utilidade.
