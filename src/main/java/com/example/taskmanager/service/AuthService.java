package com.example.taskmanager.service;

import com.example.taskmanager.dto.LoginRequestDTO;
import com.example.taskmanager.dto.LoginResponseDTO;
import com.example.taskmanager.dto.RegistroRequestDTO;
import com.example.taskmanager.dto.UsuarioResponseDTO;

public interface AuthService {
    UsuarioResponseDTO registrar(RegistroRequestDTO registroRequestDTO);
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
