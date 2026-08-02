# ADR 001: Paradigma RESTful

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
O projeto está migrando de uma aplicação local monoposto baseada em JavaFX e banco de dados SQLite para uma arquitetura multiusuário escalável e baseada na web. 

## Decisão
Decidimos utilizar o paradigma REST (Representational State Transfer) para a comunicação entre cliente e servidor. A API será desenvolvida estritamente *stateless*, onde cada requisição deve conter todas as informações necessárias para ser processada, sem depender de estado mantido em sessão no servidor.

## Consequências
*   **Positivas:** Total desacoplamento entre frontend e backend, escalabilidade horizontal simplificada (servidores sem estado), facilidade de evoluir clientes Web/Mobile de forma independente.
*   **Negativas:** Exige o envio repetido de informações de autenticação (JWT) em cada requisição, aumentando o overhead de rede por chamada.
