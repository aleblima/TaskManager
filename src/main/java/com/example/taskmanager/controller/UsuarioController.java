package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioResponseDTO response = usuarioService.obterUsuarioAtual(username);
        return ResponseEntity.ok(response);
    }
}
