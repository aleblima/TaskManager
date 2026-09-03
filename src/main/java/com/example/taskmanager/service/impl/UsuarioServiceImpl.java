package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.entity.Usuario;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.UsuarioMapper;
import com.example.taskmanager.repository.UsuarioRepository;
import com.example.taskmanager.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;


    @Override
    public UsuarioResponseDTO obterUsuarioAtual(String usernameAutenticado) {
    Usuario usuario = usuarioRepository
        .findByUsername(usernameAutenticado)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return usuarioMapper.toDTO(usuario);
    }
}
