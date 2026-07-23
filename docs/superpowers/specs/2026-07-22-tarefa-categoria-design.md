# Design: Entidades Tarefa e Categoria

**Data:** 2026-07-22

## Objetivo

Criar as entidades `Tarefa` e `Categoria` para o sistema de gerenciamento de tarefas, seguindo o design unidirecional aprovado.

## Entidades

### Tarefa

| Campo | Tipo | Anotação | Observação |
|-------|------|----------|------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` | Chave primária auto-increment |
| titulo | String | `@Column(nullable=false)` | Obrigatório |
| descricao | String | `@Column(columnDefinition="TEXT")` | Opcional, pode ser longo |
| concluida | Boolean | `@Column(nullable=false)` | Padrão: `false` |
| dataCriacao | LocalDateTime | `@Column(nullable=false)` | Auto-preenchida: `LocalDateTime.now()` |
| usuario | Usuario | `@ManyToOne(fetch=LAZY)` + `@JoinColumn(nullable=false)` | Dono da tarefa |
| categoria | Categoria | `@ManyToOne(fetch=LAZY)` + `@JoinColumn(nullable=false)` | Categoria da tarefa |

### Categoria

| Campo | Tipo | Anotação | Observação |
|-------|------|----------|------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` | Chave primária auto-increment |
| nome | String | `@Column(nullable=false, unique=true)` | Case-insensitive (tratar no service) |

## Relacionamentos

```
Tarefa ──ManyToOne──► Usuario
Tarefa ──ManyToOne──► Categoria
```

- **Unidirecional**: Apenas `Tarefa` referencia as outras entidades
- `Usuario` e `Categoria` **NÃO** possuem `@OneToMany` para `Tarefa`
- `FetchType.LAZY` em todos os relacionamentos

## Mudanças em Entidades Existentes

### Usuario.java

**Remover** as linhas 29-32:
```java
@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private List<Tarefa> tarefas = new ArrayList<>();
```

**Remover** os imports não utilizados:
```java
import java.util.ArrayList;
import java.util.List;
```

## Padrões a Seguir

- Seguir estilo do `Usuario.java` (Lombok, JPA annotations)
- `FetchType.LAZY` para todos os relacionamentos
- `cascade = CascadeType.ALL` não necessário (unidirecional)
- Inicializar `concluida = false` e `dataCriacao = LocalDateTime.now()` diretamente no campo

## Arquivos a Criar/Modificar

1. **Criar**: `src/main/java/com/example/taskmanager/entity/Categoria.java`
2. **Criar**: `src/main/java/com/example/taskmanager/entity/Tarefa.java`
3. **Modificar**: `src/main/java/com/example/taskmanager/entity/Usuario.java` (remover @OneToMany)
