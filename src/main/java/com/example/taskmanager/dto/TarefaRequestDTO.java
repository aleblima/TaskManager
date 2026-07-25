package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TarefaRequestDTO(

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 70, message = "Título deve ter no máximo 70 caracteres")
    String titulo,

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    String descricao,

    @NotBlank(message = "Categoria é obrigatória")
    String categoriaNome
) {}
