package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Categoria;
import com.example.taskmanager.entity.Tarefa;
import com.example.taskmanager.entity.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TarefaRepositoryTest {

    @Autowired private TarefaRepository tarefaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private EntityManager entityManager;

    @Test
    void findByUsuarioId_retorna_somente_tarefas_do_usuario_com_relacoes_carregadas() {
        Usuario ana = usuarioRepository.save(new Usuario(null, "Ana", "ana12345", "senha"));
        Usuario bruno = usuarioRepository.save(new Usuario(null, "Bruno", "bruno123", "senha"));
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Estudos"));
        tarefaRepository.save(new Tarefa(null, "Estudar", null, false, LocalDateTime.now(), ana, categoria));
        tarefaRepository.save(new Tarefa(null, "Outra", null, false, LocalDateTime.now(), bruno, categoria));

        entityManager.flush();
        entityManager.clear();

        List<Tarefa> tarefas = tarefaRepository.findByUsuarioId(ana.getId());

        assertEquals(1, tarefas.size());
        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(tarefas.getFirst(), "usuario"));
        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(tarefas.getFirst(), "categoria"));
    }

    @Test
    void findByIdComRelacionamentos_carrega_usuario_e_categoria() {
        Usuario ana = usuarioRepository.save(new Usuario(null, "Ana", "ana12345", "senha"));
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Estudos"));
        Tarefa salva = tarefaRepository.save(new Tarefa(null, "Estudar", null, false, LocalDateTime.now(), ana, categoria));

        entityManager.flush();
        entityManager.clear();

        Tarefa tarefa = tarefaRepository.findByIdComRelacionamentos(salva.getId()).orElseThrow();

        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(tarefa, "usuario"));
        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(tarefa, "categoria"));
    }
}
