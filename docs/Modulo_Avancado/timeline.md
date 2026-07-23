Estrutura Maven recriada do zero via Spring Initializr (não um pom.xml 
editado), por causa da profundidade da mudança de paradigma.

Nova branch dedicada: Spring-Migration.

Dependências do Initializr: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, driver PostgreSQL, (devtools e lombok opcionais).
Dependências adicionadas manualmente:
JWT: JJWT (jjwt-api, jjwt-impl, jjwt-jackson, versão 0.12.6) — escolhido no lugar do Nimbus por ser mais simples de aprender.
Swagger: springdoc-openapi-starter-webmvc-ui, versão 2.5.0.
Testes: spring-boot-starter-test já traz JUnit 5 + Mockito + AssertJ 
(dependências manuais antigas de JUnit podem ser removidas). 