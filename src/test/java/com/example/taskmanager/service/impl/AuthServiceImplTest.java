package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.LoginRequestDTO;
import com.example.taskmanager.dto.LoginResponseDTO;
import com.example.taskmanager.dto.RegistroRequestDTO;
import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.entity.Usuario;
import com.example.taskmanager.exception.CredenciaisInvalidasException;
import com.example.taskmanager.exception.RegraDeNegocioException;
import com.example.taskmanager.mapper.UsuarioMapper;
import com.example.taskmanager.repository.UsuarioRepository;
import com.example.taskmanager.security.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class AuthServiceImplTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final UsuarioMapper usuarioMapper = new UsuarioMapper();
    private final AuthServiceImpl authService = new AuthServiceImpl(
            usuarioRepository,
            passwordEncoder,
            jwtTokenProvider,
            usuarioMapper);

    @Test
    void registrar_usuario_novo_retorna_dto() {
        RegistroRequestDTO request = new RegistroRequestDTO("Ana", "ana12345", "senha123");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senha123cripto");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponseDTO resposta = authService.registrar(request);

        assertNotNull(resposta);
        assertEquals("Ana", resposta.nome());
        assertEquals("ana12345", resposta.username());
        ArgumentCaptor<Usuario> usuarioSalvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioSalvo.capture());
        assertEquals("senha123cripto", usuarioSalvo.getValue().getSenha());
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void registrar_username_existente_lanca_excecao() {
        RegistroRequestDTO request = new RegistroRequestDTO("Ana", "ana12345", "senha123");
        Usuario existente = new Usuario(1L, "Outro", "ana12345", "outra");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(existente));

        assertThrows(RegraDeNegocioException.class, () -> authService.registrar(request));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_credenciais_validas_retorna_token() {
        LoginRequestDTO request = new LoginRequestDTO("ana12345", "senha123");
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha123cripto");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "senha123cripto")).thenReturn(true);
        when(jwtTokenProvider.gerarToken("ana12345")).thenReturn("token-jwt");

        LoginResponseDTO resposta = authService.login(request);

        assertNotNull(resposta);
        assertEquals("token-jwt", resposta.token());
        assertEquals("Ana", resposta.nome());
    }

    @Test
    void login_usuario_inexistente_lanca_excecao() {
        LoginRequestDTO request = new LoginRequestDTO("naoexiste", "senha123");
        when(usuarioRepository.findByUsername("naoexiste")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class, () -> authService.login(request));
    }

    @Test
    void login_senha_incorreta_lanca_excecao() {
        LoginRequestDTO request = new LoginRequestDTO("ana12345", "errada");
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha123cripto");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "senha123cripto")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class, () -> authService.login(request));
    }
}
