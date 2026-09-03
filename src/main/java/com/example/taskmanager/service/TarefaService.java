package com.example.taskmanager.service;

import com.example.taskmanager.dto.TarefaRequestDTO;
import com.example.taskmanager.dto.TarefaResponseDTO;

import java.util.List;

public interface TarefaService {

    TarefaResponseDTO criar(TarefaRequestDTO tarefaRequest, String usernameAutenticado);
    List<TarefaResponseDTO> listarTodas(String usernameAutenticado);
    TarefaResponseDTO buscarPorId(Long id, String usernameAutenticado);
    TarefaResponseDTO atualizar(Long id, TarefaRequestDTO tarefaRequest, String usernameAutenticado);
    TarefaResponseDTO marcarComoConcluida(Long id, String usernameAutenticado);
    void deletar(Long id, String usernameAutenticado);
    TarefaResponseDTO reabrir(Long id, String usernameAutenticado);
}
