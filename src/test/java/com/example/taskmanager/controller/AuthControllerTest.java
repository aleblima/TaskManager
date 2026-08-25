package com.example.taskmanager.controller;

import com.example.taskmanager.dto.LoginRequestDTO;
import com.example.taskmanager.dto.LoginResponseDTO;
import com.example.taskmanager.dto.RegistroRequestDTO;
import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.exception.CredenciaisInvalidasException;
import com.example.taskmanager.exception.RegraDeNegocioException;
import com.example.taskmanager.security.JwtAuthenticationFilter;
import com.example.taskmanager.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.taskmanager.exception.GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registro_dados_validos_retorna_201() throws Exception {
        RegistroRequestDTO request = new RegistroRequestDTO("Ana", "ana12345", "senha123");
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Ana", "ana12345");
        when(authService.registrar(any(RegistroRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.username").value("ana12345"));
    }

    @Test
    void registro_username_existente_retorna_409() throws Exception {
        RegistroRequestDTO request = new RegistroRequestDTO("Ana", "ana12345", "senha123");
        when(authService.registrar(any(RegistroRequestDTO.class)))
                .thenThrow(new RegraDeNegocioException("ana12345 está em uso"));

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void registro_dados_invalidos_retorna_400() throws Exception {
        RegistroRequestDTO request = new RegistroRequestDTO("", "ab", "12");

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void login_credenciais_validas_retorna_200() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("ana12345", "senha123");
        LoginResponseDTO response = new LoginResponseDTO("token-jwt", "Ana");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void login_credenciais_invalidas_retorna_401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("ana12345", "errada");
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new CredenciaisInvalidasException("Credenciais inválidas"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
