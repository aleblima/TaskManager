package com.example.taskmanager.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.taskmanager.entity.Categoria;
import com.example.taskmanager.entity.Tarefa;
import com.example.taskmanager.entity.Usuario;
import org.junit.jupiter.api.Test;

class TarefaMapperTest {

    @Test
    void toDTO_expoe_nome_do_usuario() {
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha");
        Categoria categoria = new Categoria(2L, "Pessoal");
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuario(usuario);
        tarefa.setCategoria(categoria);

        assertEquals("Ana", new TarefaMapper().toDTO(tarefa).usuarioNome());
    }
}
