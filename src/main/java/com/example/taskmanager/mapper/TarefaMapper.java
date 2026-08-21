package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.TarefaRequestDTO;
import com.example.taskmanager.dto.TarefaResponseDTO;
import com.example.taskmanager.entity.Categoria;
import com.example.taskmanager.entity.Tarefa;
import com.example.taskmanager.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class TarefaMapper {

    public TarefaResponseDTO toDTO(Tarefa tarefa){
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getConcluida(),
                tarefa.getDataCriacao(),
                tarefa.getUsuario().getId(),
                tarefa.getCategoria().getId(),
                tarefa.getCategoria().getNome());
    }

    public Tarefa toEntity(TarefaRequestDTO dto, Usuario usuario, Categoria categoria){
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setUsuario(usuario);
        tarefa.setCategoria(categoria);
        return tarefa;
    }
}
