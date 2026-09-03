# Design: Integração Contínua com GitHub Actions

**Data:** 2026-09-02

## Problema

O repositório não valida automaticamente uma alteração antes de ela ser
incorporada à `main`. Embora o Maven já execute testes, ArchUnit e a regra de
cobertura mínima de 90%, essa validação depende de execução manual e não
impede que uma regressão seja integrada por Pull Request.

## Solução

Adotar GitHub Actions como a esteira de integração contínua do repositório. A
esteira será executada exclusivamente quando um Pull Request tiver `main` como
branch de destino; o ruleset existente já proíbe push direto nessa branch.

O workflow não entrega nem publica a aplicação: não há CD, ambiente de deploy,
segredos de ambiente ou banco externo nesta decisão.

## Histórias de Usuário

1. Como mantenedor, quero que cada Pull Request para `main` execute a suíte
   Maven para impedir o merge de regressões.
2. Como mantenedor, quero bloquear dependências novas com vulnerabilidades
   conhecidas antes de incorporá-las ao projeto.
3. Como mantenedor, quero receber alertas de possíveis vulnerabilidades no
   código Java sem tornar alertas ainda não triados um bloqueio de merge.
4. Como colaborador, quero consultar o relatório JaCoCo da execução para
   investigar uma falha de cobertura sem reproduzir a CI localmente.

## Decisões de Implementação

### Evento, permissões e ambiente

- Criar um workflow versionado em `.github/workflows/ci.yml`.
- Dispará-lo somente em `pull_request` cujo destino seja `main`, incluindo a
  abertura, reabertura e cada atualização de um Pull Request.
- Executar jobs em `ubuntu-latest` e usar Java 21.
- Declarar permissões mínimas: leitura de conteúdo como padrão; o job CodeQL
  recebe adicionalmente a permissão estritamente necessária para publicar seus
  resultados de análise.
- Configurar `persist-credentials: false` em cada checkout, pois nenhum job
  precisa executar operações Git autenticadas após baixar o código do PR.
- Não usar `pull_request_target`, segredos, banco PostgreSQL, Docker nem
  credenciais externas. Os testes usam H2 no perfil de teste.

### Job obrigatório `test`

- Fazer checkout do código avaliado no Pull Request.
- Configurar Java 21 e o cache de dependências Maven.
- Executar `sh ./mvnw test`, pois o script versionado não possui bit de
  execução e o comando deve funcionar no runner Linux sem depender dessa
  permissão de arquivo.
- Manter o nome do job como `test`, pois esse será o status check selecionado
  como obrigatório no ruleset de `main`.
- O comando deve cobrir compilação, todos os testes existentes, regras
  ArchUnit e a verificação JaCoCo de pelo menos 90% de linhas, já configurada
  no `pom.xml`.

### Job obrigatório `dependency-review`

- Executar a ação oficial de revisão de dependências do GitHub no mesmo evento
  de Pull Request e limitar o escopo a PRs para `main`.
- Configurar o job para falhar quando o PR introduzir uma dependência com
  vulnerabilidade conhecida de severidade `high` ou `critical`, em escopos de
  desenvolvimento, runtime ou desconhecido.
- Manter o nome do job como `dependency-review`, para que ele seja selecionado
  como obrigatório no ruleset de `main` após a primeira execução.
- Antes de torná-lo obrigatório, habilitar o Dependency graph no repositório e
  confirmar que o recurso está disponível para o plano e a visibilidade do
  repositório no GitHub.

### Job informativo `codeql`

- Inicializar e analisar somente a linguagem Java com CodeQL.
- Publicar resultados na área **Security** do GitHub, sem selecionar o job
  `codeql` como status check obrigatório no ruleset nesta etapa.
- Usar o modo de análise sem build adicional para Java, evitando repetir a
  execução Maven do job `test`.
- Falhas operacionais do job devem ficar visíveis no Pull Request; alertas de
  segurança encontrados pela análise são tratados como achados para triagem,
  não como motivo automático para bloquear merge.

### Relatório de cobertura

- No job `test`, publicar `target/site/jacoco/` como artefato da execução,
  inclusive se a etapa de testes falhar após gerar o relatório.
- A inexistência do diretório de relatório não deve mascarar a causa primária
  da falha, por exemplo uma falha de compilação.
- O artefato é apenas diagnóstico; a regra que bloqueia cobertura continua
  sendo o `jacoco-maven-plugin` no Maven.

### Ruleset e documentação

- Depois que o workflow chegar à branch padrão e seus jobs tiverem executado ao
  menos uma vez, configurar o ruleset da `main` para exigir `test` e
  `dependency-review` antes do merge.
- Não exigir `codeql` nem o upload do relatório como checks de merge nesta
  etapa.
- Documentar no README como a CI funciona, onde inspecionar as execuções e
  quais checks o ruleset exige.

## Decisões de Teste e Validação

- Validar o YAML por uma Pull Request de teste dirigida a `main`; não criar
  testes Java somente para exercitar a infraestrutura de CI.
- Confirmar que `test` é bem-sucedido em uma alteração que preserva a suíte e
  falha quando `./mvnw test` falha, inclusive por cobertura JaCoCo abaixo de
  90%.
- Confirmar que `dependency-review` detecta a alteração de uma dependência
  vulnerável em um PR de teste, quando o recurso estiver disponível, sem fazer
  merge dessa alteração.
- Confirmar que CodeQL publica uma execução e resultados ou a ausência deles
  na área Security sem ser exigido pelo ruleset.
- Confirmar que, após selecionar os dois checks no ruleset, GitHub bloqueia o
  merge de um PR enquanto `test` ou `dependency-review` estiver falhando,
  pendente ou ausente.

## Fora do Escopo

- Deploy, CD, ambientes GitHub, publicação de artefatos de aplicação ou
  containerização da API.
- Execução em push direto para `main` ou para branches de trabalho.
- Formatação automática, linter adicional, SonarQube, Dependabot ou mudança de
  dependências Maven.
- Tornar alertas do CodeQL bloqueantes antes da triagem inicial.

## Critérios de Aceite

1. Todo Pull Request cujo destino é `main` aciona o workflow de CI.
2. O job `test` executa `sh ./mvnw test` com Java 21 e falha quando a suíte,
   ArchUnit ou a cobertura JaCoCo falhar.
3. O job `dependency-review` bloqueia a introdução de dependência vulnerável
   de severidade high ou critical, se o Dependency graph estiver disponível.
4. CodeQL analisa Java e publica os achados sem ser requisito de merge.
5. O relatório JaCoCo pode ser baixado como artefato quando gerado.
6. O ruleset de `main` exige a aprovação de `test` e `dependency-review` antes
   de permitir merge por Pull Request.
