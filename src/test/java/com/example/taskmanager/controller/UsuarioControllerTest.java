package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.security.JwtAuthenticationFilter;
import com.example.taskmanager.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.taskmanager.exception.GlobalExceptionHandler.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "ana12345")
    void me_usuario_autenticado_retorna_200() throws Exception {
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Ana", "ana12345");
        when(usuarioService.obterUsuarioAtual("ana12345")).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.username").value("ana12345"));

        verify(usuarioService).obterUsuarioAtual("ana12345");
    }

    @Test
    @WithMockUser(username = "naoexiste")
    void me_usuario_nao_encontrado_retorna_404() throws Exception {
        when(usuarioService.obterUsuarioAtual("naoexiste"))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isNotFound());
    }
}
