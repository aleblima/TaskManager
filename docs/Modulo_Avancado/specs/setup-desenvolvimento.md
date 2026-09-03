# Design: Setup de Desenvolvimento e Inicialização da API

**Data:** 2026-08-22

## Problema

O projeto já possui entidades, repositórios, serviços e configuração básica da API, mas ainda não dispõe de um fluxo local confiável para iniciar a aplicação. O Maven Wrapper falha antes de executar o Maven, o PostgreSQL depende de preparação externa não documentada e o README ainda descreve a antiga aplicação JavaFX com SQLite.

## Solução

Disponibilizar um ambiente de desenvolvimento reproduzível em que Docker Compose inicia somente o PostgreSQL 17 e a API Spring Boot é executada no host pelo Maven Wrapper. O Hibernate deve criar e remover o schema automaticamente, conforme ADR-008, sem substituir PostgreSQL por H2 no ambiente de desenvolvimento.

## Histórias de Usuário

1. Como desenvolvedor, quero iniciar o PostgreSQL com um único comando para não configurar manualmente o banco local.
2. Como desenvolvedor, quero executar a API com o Maven Wrapper para usar uma versão de Maven consistente no projeto.
3. Como desenvolvedor, quero que o banco `taskmanager` exista ao iniciar o container para que a API consiga se conectar imediatamente.
4. Como desenvolvedor, quero sobrescrever credenciais e configurações de conexão por variáveis de ambiente para adequar o setup à minha máquina sem alterar arquivos versionados.
5. Como desenvolvedor, quero manter H2 apenas nos testes para que o ambiente local valide o mapeamento JPA contra PostgreSQL.
6. Como desenvolvedor, quero instruções atualizadas no README para conseguir iniciar o ambiente sem consultar a implementação.

## Decisões de Implementação

### Execução da aplicação

- A classe principal mantém `@SpringBootApplication` e não recebe alterações nesta etapa: ela já é suficiente para varrer os componentes sob `com.example.taskmanager` e iniciar o contexto Spring.
- O comando oficial de compilação é `mvnw.cmd -DskipTests compile`.
- O comando oficial de inicialização é `mvnw.cmd spring-boot:run`.
- O Maven Wrapper deve receber apenas a correção necessária para tratar corretamente o caso em que a propriedade de destino do diretório Maven não existe. Não regenerar os scripts do wrapper nesta etapa.

### PostgreSQL com Docker Compose

- O Compose deve conter somente o serviço PostgreSQL; a API Java não será conteinerizada nesta etapa.
- A imagem deve ser fixada em PostgreSQL 17, sem usar a tag `latest`.
- A porta do container deve ser publicada exclusivamente em
  `127.0.0.1:5433`, impedindo acesso de outros hosts à instância local.
- Os valores padrão são banco `taskmanager`, usuário `postgres` e senha `postgres`.
- As configurações do banco no Compose devem aceitar sobrescrita por variáveis de ambiente, preservando os valores padrão acima.
- O serviço deve usar um volume nomeado para o diretório de dados do PostgreSQL. A remoção do volume é o mecanismo explícito para recriar o banco do zero.

### Configuração Spring e dependências

- `application.properties` continua usando PostgreSQL como datasource principal e preserva `spring.jpa.hibernate.ddl-auto=create-drop`.
- `DB_USERNAME` e `DB_PASSWORD` continuam sendo a forma de sobrescrever as
  credenciais lidas pela aplicação; `POSTGRES_USER` e `POSTGRES_PASSWORD`
  configuram o container. Quando personalizados, os pares devem receber valores
  equivalentes para a aplicação autenticar no banco criado pelo Compose.
- H2 e `application-test.properties` permanecem exclusivos ao escopo de testes.
- Atualizar Springdoc para a versão fixa `3.0.2`, compatível com a linha Spring Boot 4.x; não introduzir configuração OpenAPI, controllers ou endpoints nesta etapa.

### Documentação

- O README deve deixar de instruir a execução JavaFX/SQLite.
- Documentar Java 21 e Docker Desktop como pré-requisitos.
- Documentar, nesta ordem, os comandos para iniciar o PostgreSQL, compilar a API e executar a aplicação.
- Documentar as variáveis para sobrescrever as credenciais padrão e como remover o volume do Compose para recriar o banco.

## Decisões de Teste e Validação

- Esta etapa não cria nem executa testes automatizados de integridade; eles serão realizados após a aplicação iniciar corretamente.
- A validação de bootstrap deve confirmar que o Maven Wrapper conclui a compilação com testes ignorados, que o Compose disponibiliza PostgreSQL somente em `127.0.0.1:5433` e que a aplicação inicia sem falha de criação de beans ou conexão.
- Os logs de inicialização devem evidenciar a conexão com PostgreSQL e a criação do schema pelo Hibernate. Ao encerrar a aplicação, o comportamento esperado de `create-drop` é remover as tabelas, sem remover o banco ou o volume do container.

## Fora do Escopo

- Controllers REST, rotas, filtros JWT e configuração de autorização HTTP.
- Containerização da aplicação Java, imagens de aplicação e execução completa da API por Docker Compose.
- Actuator, endpoint de health check, observabilidade e seed de dados.
- Alteração das regras de domínio, entidades, repositórios, DTOs, services ou mappers.
- Migração de H2 para o ambiente de desenvolvimento.

## Critérios de Aceite

1. O PostgreSQL 17 pode ser iniciado pelo Compose e aceita conexões em `127.0.0.1:5433` usando os valores padrão, sem publicar a porta em outras interfaces.
2. O Maven Wrapper deixa de falhar durante a descoberta ou download do Maven.
3. A compilação por `mvnw.cmd -DskipTests compile` é concluída.
4. A execução por `mvnw.cmd spring-boot:run` inicia o contexto Spring conectado ao PostgreSQL.
5. O schema é criado pelo Hibernate durante a inicialização e removido quando a aplicação é encerrada.
6. O README descreve exclusivamente o fluxo atual da API Spring Boot.
