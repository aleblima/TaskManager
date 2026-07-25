package com.example.taskmanager.dto;

import java.time.LocalDateTime;

public record TarefaResponseDTO(
    Long id,
    String titulo,
    String descricao,
    Boolean concluida,
    LocalDateTime dataCriacao,
    Long usuarioId,
    Long categoriaId,
    String categoriaNome
) {}
