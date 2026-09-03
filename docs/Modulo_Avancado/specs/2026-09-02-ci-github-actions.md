# GitHub Actions CI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bloquear merges de Pull Requests para `main` quando testes ou revisão de dependências falharem, mantendo CodeQL e o relatório JaCoCo como diagnósticos.

**Architecture:** Um workflow único, `../../../.github/workflows/ci.yml`, é acionado apenas por `pull_request` para `main`. Seus jobs independentes são `test`, `dependency-review` e `codeql`; somente os dois primeiros serão configurados posteriormente no ruleset. O Maven continua sendo a fonte de verdade da qualidade Java, e o GitHub apenas executa e apresenta seus resultados.

**Tech Stack:** GitHub Actions, Java 21, Maven Wrapper 3.3.4, distribuição Maven 3.9.16, JaCoCo, GitHub Dependency Review, GitHub CodeQL.

**Spec:** `ci-github-actions-design.md`

## Global Constraints

- Acionar exclusivamente em Pull Requests cujo destino é `main`; não adicionar gatilhos `push`, `workflow_dispatch` ou `pull_request_target`.
- Usar runners `ubuntu-latest`, Java 21 e o Maven Wrapper por `sh ./mvnw`,
  pois o script versionado não possui bit de execução.
- Não usar segredos, PostgreSQL, Docker, CD ou permissões de escrita além de `security-events: write` no job CodeQL.
- Preservar `test` e `dependency-review` como nomes estáveis de jobs obrigatórios; `codeql` permanece informativo.
- Manter JaCoCo no Maven como única regra de cobertura; o artefato da CI é apenas diagnóstico.
- Não criar testes Java para YAML de CI. A validação do workflow é a execução observável no Pull Request de bootstrap.
- Não fazer commit automaticamente; o commit é responsabilidade do usuário.

---

## Estrutura de arquivos

- Criar `../../../.github/workflows/ci.yml`: workflow de CI com os três jobs e o upload do relatório JaCoCo.
- Modificar `../../../README.md`: explicar o gatilho, os checks, o relatório de cobertura e a configuração única do ruleset.
- Criar `docs/superpowers/plans/2026-09-02-ci-github-actions.md`: este roteiro de implementação e bootstrap.

### Task 1: Versionar o workflow de CI

**Files:**
- Create: `../../../.github/workflows/ci.yml`
- Test: execução automática em Pull Request para `main`, observada na aba **Checks** do GitHub.

**Interfaces:**
- Consumes: `../../../pom.xml`, que associa testes, ArchUnit e JaCoCo à fase Maven `test`.
- Produces: checks GitHub Actions chamados `test`, `dependency-review` e `codeql`; artefato `jacoco-report`.

- [ ] **Step 1: Criar o workflow com o gatilho e permissões mínimas**

```yaml
name: CI

on:
  pull_request:
    branches: [main]

permissions:
  contents: read
```

- [ ] **Step 2: Acrescentar o job obrigatório `test`**

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - name: Run Maven tests
        run: sh ./mvnw test
      - name: Upload JaCoCo report
        if: ${{ always() }}
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/
          if-no-files-found: ignore
          retention-days: 7
```

- [ ] **Step 3: Acrescentar o job obrigatório `dependency-review`**

```yaml
  dependency-review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
      - uses: actions/dependency-review-action@v4
        with:
          fail-on-severity: high
          fail-on-scopes: development, runtime, unknown
```

- [ ] **Step 4: Acrescentar o job informativo `codeql`**

```yaml
  codeql:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      security-events: write
    steps:
      - uses: actions/checkout@v6
      - uses: github/codeql-action/init@v4
        with:
          languages: java
          build-mode: none
      - uses: github/codeql-action/analyze@v4
        with:
          category: /language:java
```

- [ ] **Step 5: Validar a estrutura local do YAML e a suíte Maven**

Run: `sh ./mvnw test`

Expected: saída Maven com sucesso e JaCoCo sem violação do mínimo de 0.90; o arquivo YAML deve conter exclusivamente o evento `pull_request` para `main`, os três jobs e nenhum uso de segredos ou `pull_request_target`.

### Task 2: Documentar a operação da CI

**Files:**
- Modify: `../../../README.md` (adicionar seção `## Integração contínua` depois de `## Testes`)
- Test: revisão do Markdown renderizado e da coerência com `../../../.github/workflows/ci.yml`.

**Interfaces:**
- Consumes: checks e artefato definidos na Task 1.
- Produces: instruções para colaboradores consultarem a execução e configurarem o ruleset após o primeiro run.

- [ ] **Step 1: Adicionar a seção de CI ao README**

```markdown
## Integração contínua

Todo Pull Request destinado a `main` executa o workflow **CI** no GitHub
Actions. Ele possui três checks:

- `test` executa `sh ./mvnw test`, incluindo ArchUnit e JaCoCo com cobertura
  mínima de 90%.
- `dependency-review` bloqueia dependências novas com vulnerabilidades high ou
  critical.
- `codeql` analisa o código Java e publica achados na aba **Security**; nesta
  etapa ele é informativo e não bloqueia merges.

O relatório HTML do JaCoCo, quando gerado, fica disponível como o artefato
`jacoco-report` na execução do workflow. Abra o Pull Request, selecione
**Checks** e abra o job com falha para consultar os logs ou baixar o artefato.

Após a primeira execução bem-sucedida, habilite o Dependency graph em
**Settings → Advanced Security** e, no ruleset aplicado à `main`, exija os
checks `test` e `dependency-review`. Não exija `codeql` nesta fase.
```

- [ ] **Step 2: Conferir os nomes documentados contra o workflow**

Run: `rg -n "test|dependency-review|codeql|jacoco-report" README.md .github/workflows/ci.yml`

Expected: os quatro nomes aparecem com a mesma grafia nos dois arquivos.

### Task 3: Bootstrap no Pull Request já aberto e ativação do ruleset

**Files:**
- Modify: configuração do ruleset da branch `main` no GitHub; não há arquivo local adicional.
- Test: Pull Request atual e um Pull Request subsequente para `main`.

**Interfaces:**
- Consumes: checks `test` e `dependency-review` criados pela Task 1 e a documentação da Task 2.
- Produces: merges para `main` bloqueados até ambos os checks aprovarem.

- [ ] **Step 1: Enviar o commit que contém o workflow para a branch do PR já aberto**

Run: `git push origin Spring-Migration`

Expected: o evento `pull_request` é atualizado e a aba **Checks** do PR mostra os jobs `test`, `dependency-review` e `codeql`. O arquivo já estava no PR antes da criação do ruleset, por isso esta é a execução de bootstrap.

- [ ] **Step 2: Inspecionar e resolver a primeira execução antes do merge**

Expected: `test` passa; o artefato `jacoco-report` é baixável quando o relatório foi gerado; `codeql` registra a análise. Se `dependency-review` informar que Dependency graph ou o recurso não está disponível, habilitar o Dependency graph em **Settings → Advanced Security** e reenviar a execução; não ignorar esse job.

- [ ] **Step 3: Configurar os checks obrigatórios no ruleset de `main`**

No GitHub, abrir **Settings → Rules → Rulesets**, editar o ruleset aplicado a `main`, habilitar **Require status checks to pass** e selecionar `test` e `dependency-review` como checks exigidos do GitHub Actions. Manter `codeql` fora da lista. Se a interface ainda não listar um check, executar novamente o workflow; o GitHub só permite selecionar checks que tiveram execução bem-sucedida recente no repositório.

- [ ] **Step 4: Confirmar o bloqueio em um Pull Request posterior**

Criar ou atualizar um Pull Request de teste para `main` e observar que o botão de merge permanece bloqueado enquanto `test` ou `dependency-review` está pendente ou falhando; confirmar que passa a ficar disponível apenas após ambos terem sucesso.

## Revisão do plano

- Cobertura da spec: Task 1 cobre evento, permissões, Java 21, os três jobs e o artefato; Task 2 cobre a documentação; Task 3 cobre Dependency graph, bootstrap e ruleset.
- Sem placeholders: todas as ações, caminhos, nomes de jobs e comandos de validação foram definidos.
- Consistência: os checks exigidos são exatamente `test` e `dependency-review`; `codeql` e `jacoco-report` permanecem informativos em todas as tarefas.
