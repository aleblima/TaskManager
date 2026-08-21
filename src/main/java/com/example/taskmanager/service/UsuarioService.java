package com.example.taskmanager.service;

import com.example.taskmanager.dto.UsuarioResponseDTO;

public interface UsuarioService {

    UsuarioResponseDTO obterUsuarioAtual(String usernameAutenticado);
}
