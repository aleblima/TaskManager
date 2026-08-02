# ADR 002: Stack Tecnológica

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
A migração de paradigma exige um ecossistema maduro para desenvolvimento corporativo rápido, seguro e altamente testável.

## Decisão
Decidimos adotar a seguinte stack tecnológica:
*   **Linguagem & Plataforma:** Java 21 (aproveitando recursos modernos como Virtual Threads e Records).
*   **Framework:** Spring Boot 4.1.0 (ecossistema maduro com excelentes starters de teste, segurança e dados).
*   **Banco Principal:** PostgreSQL (SGBD relacional robusto para produção).
*   **Banco de Testes:** H2 em memória (para testes rápidos de integração e arquitetura).
*   **Build & Dependências:** Maven.
*   **Bibliotecas de Produtividade:** Lombok (para redução de boilerplate) e Devtools.

## Consequências
*   **Positivas:** Curva de aprendizado estabelecida na indústria, alta performance com Java 21, excelente isolamento de testes de integração com H2.
*   **Negativas:** Spring Boot tem tempo de inicialização maior se comparado a runtimes nativos ou microsserviços Go/Node.js.
