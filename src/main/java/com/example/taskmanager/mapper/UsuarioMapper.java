package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.RegistroRequestDTO;
import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public UsuarioResponseDTO toDTO (Usuario usuario){
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername());
    }

    public Usuario toEntity(RegistroRequestDTO dto){
        return new Usuario(
                null,
                dto.nome(),
                dto.username(), null);
    }
}
