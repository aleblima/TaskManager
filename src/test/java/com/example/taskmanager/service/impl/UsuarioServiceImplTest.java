package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.entity.Usuario;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.UsuarioMapper;
import com.example.taskmanager.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceImplTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioMapper usuarioMapper = new UsuarioMapper();
    private final UsuarioServiceImpl usuarioService = new UsuarioServiceImpl(usuarioMapper, usuarioRepository);

    @Test
    void obterUsuarioAtual_retorna_dto() {
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO resposta = usuarioService.obterUsuarioAtual("ana12345");

        assertNotNull(resposta);
        assertEquals(1L, resposta.id());
        assertEquals("Ana", resposta.nome());
        assertEquals("ana12345", resposta.username());
    }

    @Test
    void obterUsuarioAtual_usuario_inexistente_lanca_excecao() {
        when(usuarioRepository.findByUsername("naoexiste")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.obterUsuarioAtual("naoexiste"));
    }
}
