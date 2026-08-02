# ADR 008: Configuração JPA e ddl-auto em Desenvolvimento e Testes

**Status:** Accepted  
**Data:** 2026-08-02  

## Contexto
Durante a fase inicial de migração e desenvolvimento acelerado baseada em SDD (Specification-Driven Development), o modelo de dados sofre alterações constantes. A manutenção manual de arquivos de migração (como Flyway ou SQL scripts) nesta fase geraria alto atrito e lentidão no desenvolvimento.

## Decisão
Decidimos utilizar a estratégia de recriação automática do schema para agilizar as iterações de desenvolvimento e manter os ambientes sempre sincronizados:
1.  **Estratégia:** Definir `spring.jpa.hibernate.ddl-auto=create-drop` para o banco de desenvolvimento local (PostgreSQL) e também para o banco de testes em memória (H2).
2.  **Ciclo de vida:** O banco de dados terá suas tabelas destruídas e recriadas a cada inicialização da aplicação em ambos os ambientes.

## Consequências
*   **Positivas:** Sincronismo perfeito entre as entidades Java e as tabelas do banco de dados de desenvolvimento em tempo de execução, zero atrito para aplicar mudanças de schema especificadas em specs.
*   **Negativas:** O banco local perde todos os dados mockados criados no teste manual a cada reinicialização da aplicação, exigindo o uso de arquivos de carga inicial (`import.sql` ou similar) se necessário persistir dados de teste.
