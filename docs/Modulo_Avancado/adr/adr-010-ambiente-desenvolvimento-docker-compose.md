# ADR 010: Ambiente de Desenvolvimento com Docker Compose

**Status:** Accepted  
**Data:** 2026-08-22  

## Contexto

A API depende de PostgreSQL para iniciar no ambiente de desenvolvimento. Exigir que cada pessoa instale, configure e mantenha manualmente uma instância local do banco torna o primeiro uso pouco reproduzível. Além disso, o Maven Wrapper é o mecanismo escolhido para padronizar a versão e a execução do Maven, mas sua configuração atual impede a inicialização.

## Decisão

Adotamos Docker Compose para disponibilizar exclusivamente o PostgreSQL de desenvolvimento, enquanto a aplicação Spring Boot continua sendo executada no host pelo Maven Wrapper.

1. O Compose utilizará uma imagem fixada em PostgreSQL 17, expondo a porta do
   host `5433` exclusivamente em `127.0.0.1`.
2. A configuração padrão criará o banco `taskmanager` com usuário e senha `postgres`, permitindo sobrescrita por variáveis de ambiente.
3. Um volume nomeado preservará os dados do serviço PostgreSQL entre reinicializações do container. O Hibernate continua responsável apenas pelo schema, usando `ddl-auto=create-drop`.
4. O Maven Wrapper será corrigido de forma pontual e será o comando oficial para compilar e executar a aplicação localmente.
5. H2 permanece restrito ao perfil de testes; não será usado como banco de desenvolvimento.

## Consequências

* **Positivas:** O banco local passa a ter uma configuração reprodutível, o onboarding reduz pré-requisitos manuais e a aplicação permanece simples de depurar no host.
* **Negativas:** O desenvolvimento passa a depender do Docker Desktop e exige iniciar o serviço PostgreSQL antes da aplicação. O volume pode precisar ser removido explicitamente quando for necessário recriar o banco do zero.
