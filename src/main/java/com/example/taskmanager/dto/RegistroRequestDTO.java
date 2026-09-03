package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequestDTO(

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 75, message = "Nome deve ter no máximo 75 caracteres")
    String nome,

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 8, max = 15, message = "Username deve ter entre 8 e 15 caracteres")
    String username,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    String senha
) {}
