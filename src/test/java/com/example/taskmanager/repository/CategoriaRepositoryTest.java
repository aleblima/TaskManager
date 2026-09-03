package com.example.taskmanager.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.taskmanager.entity.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void salvar_mesma_chave_normalizada_rejeita_duplicidade() {
        categoriaRepository.saveAndFlush(new Categoria(null, "Trabalho", "trabalho"));

        assertThrows(DataIntegrityViolationException.class, () ->
                categoriaRepository.saveAndFlush(new Categoria(null, "TRABALHO", "trabalho")));
    }
}
