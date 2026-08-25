package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TarefaRequestDTO;
import com.example.taskmanager.dto.TarefaResponseDTO;
import com.example.taskmanager.exception.AccessDeniedException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.security.JwtAuthenticationFilter;
import com.example.taskmanager.service.TarefaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.taskmanager.exception.GlobalExceptionHandler.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TarefaResponseDTO tarefaResponse() {
        return new TarefaResponseDTO(1L, "Estudar", "Estudar Spring", false,
                LocalDateTime.of(2026, 8, 25, 10, 0), 1L, 1L, "Estudos");
    }

    @Test
    @WithMockUser(username = "ana12345")
    void criar_dados_validos_retorna_201() throws Exception {
        TarefaRequestDTO request = new TarefaRequestDTO("Estudar", "Estudar Spring", "Estudos");
        when(tarefaService.criar(any(TarefaRequestDTO.class), eq("ana12345")))
                .thenReturn(tarefaResponse());

        mockMvc.perform(post("/api/v1/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Estudar"));
    }

    @Test
    @WithMockUser(username = "ana12345")
    void criar_dados_invalidos_retorna_400() throws Exception {
        TarefaRequestDTO request = new TarefaRequestDTO("", null, "");

        mockMvc.perform(post("/api/v1/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "ana12345")
    void listarTodas_retorna_lista() throws Exception {
        when(tarefaService.listarTodas("ana12345"))
                .thenReturn(List.of(tarefaResponse()));

        mockMvc.perform(get("/api/v1/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estudar"));
    }

    @Test
    @WithMockUser(username = "ana12345")
    void buscarPorId_tarefa_existente_retorna_200() throws Exception {
        when(tarefaService.buscarPorId(1L, "ana12345"))
                .thenReturn(tarefaResponse());

        mockMvc.perform(get("/api/v1/tarefas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "ana12345")
    void buscarPorId_tarefa_inexistente_retorna_404() throws Exception {
        when(tarefaService.buscarPorId(99L, "ana12345"))
                .thenThrow(new ResourceNotFoundException("Tarefa não encontrada"));

        mockMvc.perform(get("/api/v1/tarefas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "ana12345")
    void buscarPorId_tarefa_alheia_retorna_404() throws Exception {
        when(tarefaService.buscarPorId(1L, "ana12345"))
                .thenThrow(new ResourceNotFoundException("Tarefa não encontrada"));

        mockMvc.perform(get("/api/v1/tarefas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "ana12345")
    void atualizar_dados_validos_retorna_200() throws Exception {
        TarefaRequestDTO request = new TarefaRequestDTO("Estudar Java", "Novo conteudo", "Estudos");
        TarefaResponseDTO response = new TarefaResponseDTO(1L, "Estudar Java", "Novo conteudo", false,
                LocalDateTime.of(2026, 8, 25, 10, 0), 1L, 1L, "Estudos");
        when(tarefaService.atualizar(eq(1L), any(TarefaRequestDTO.class), eq("ana12345")))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudar Java"));
    }

    @Test
    @WithMockUser(username = "ana12345")
    void atualizar_tarefa_alheia_retorna_403() throws Exception {
        TarefaRequestDTO request = new TarefaRequestDTO("Estudar", "Desc", "Cat");
        when(tarefaService.atualizar(eq(1L), any(TarefaRequestDTO.class), eq("ana12345")))
                .thenThrow(new AccessDeniedException("Acesso negado"));

        mockMvc.perform(put("/api/v1/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ana12345")
    void concluir_tarefa_retorna_200() throws Exception {
        TarefaResponseDTO response = new TarefaResponseDTO(1L, "Estudar", "Desc", true,
                LocalDateTime.of(2026, 8, 25, 10, 0), 1L, 1L, "Cat");
        when(tarefaService.marcarComoConcluida(1L, "ana12345")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tarefas/1/concluir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    @WithMockUser(username = "ana12345")
    void concluir_tarefa_alheia_retorna_403() throws Exception {
        when(tarefaService.marcarComoConcluida(1L, "ana12345"))
                .thenThrow(new AccessDeniedException("Acesso negado"));

        mockMvc.perform(patch("/api/v1/tarefas/1/concluir"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ana12345")
    void reabrir_tarefa_retorna_200() throws Exception {
        TarefaResponseDTO response = new TarefaResponseDTO(1L, "Estudar", "Desc", false,
                LocalDateTime.of(2026, 8, 25, 10, 0), 1L, 1L, "Cat");
        when(tarefaService.reabrir(1L, "ana12345")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tarefas/1/reabrir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(false));
    }

    @Test
    @WithMockUser(username = "ana12345")
    void deletar_tarefa_retorna_204() throws Exception {
        mockMvc.perform(delete("/api/v1/tarefas/1"))
                .andExpect(status().isNoContent());

        verify(tarefaService).deletar(1L, "ana12345");
    }

    @Test
    @WithMockUser(username = "ana12345")
    void deletar_tarefa_alheia_retorna_403() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado"))
                .when(tarefaService).deletar(1L, "ana12345");

        mockMvc.perform(delete("/api/v1/tarefas/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ana12345")
    void concluir_sem_body_retorna_200() throws Exception {
        TarefaResponseDTO response = new TarefaResponseDTO(1L, "Estudar", "Desc", true,
                LocalDateTime.of(2026, 8, 25, 10, 0), 1L, 1L, "Cat");
        when(tarefaService.marcarComoConcluida(1L, "ana12345")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tarefas/1/concluir")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tarefaService).marcarComoConcluida(1L, "ana12345");
    }
}
