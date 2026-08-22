package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.TarefaRequestDTO;
import com.example.taskmanager.dto.TarefaResponseDTO;
import com.example.taskmanager.entity.Categoria;
import com.example.taskmanager.entity.Tarefa;
import com.example.taskmanager.entity.Usuario;
import com.example.taskmanager.exception.AccessDeniedException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TarefaMapper;
import com.example.taskmanager.repository.CategoriaRepository;
import com.example.taskmanager.repository.TarefaRepository;
import com.example.taskmanager.repository.UsuarioRepository;
import com.example.taskmanager.service.TarefaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TarefaServiceImpl implements TarefaService {

    private final TarefaMapper tarefaMapper;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;


    private Categoria buscarOuCriarCategoria(String nomeCategoria){
        return categoriaRepository.findByNomeIgnoreCase(nomeCategoria)
                .orElseGet(() -> {
                    Categoria novaCategoria = new Categoria();
                    novaCategoria.setNome(nomeCategoria);
                    return categoriaRepository.save(novaCategoria);
                });
    }
    @Override
    @Transactional
    public TarefaResponseDTO criar(TarefaRequestDTO tarefaRequest, String usernameAutenticado) {
    Usuario usuario = usuarioRepository
            .findByUsername(usernameAutenticado)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    Categoria categoria = buscarOuCriarCategoria(tarefaRequest.categoriaNome());
    Tarefa tarefa = tarefaMapper.toEntity(tarefaRequest, usuario, categoria);
    Tarefa tarefaSalva = tarefaRepository.save(tarefa);
    return tarefaMapper.toDTO(tarefaSalva);

    }

    @Override
    public List<TarefaResponseDTO> listarTodas(String usernameAutenticado) {

        Usuario usuario = usuarioRepository
                .findByUsername(usernameAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        List<Tarefa> lista = tarefaRepository.findByUsuarioId(usuario.getId());
        return lista.stream().map(tarefaMapper::toDTO).toList();
    }

    @Override
    public TarefaResponseDTO buscarPorId(Long id, String usernameAutenticado) {
        Tarefa tarefa = tarefaRepository
                .findByIdComRelacionamentos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        if (!tarefa.getUsuario().getUsername().equals(usernameAutenticado)) {
            throw new ResourceNotFoundException("Tarefa não encontrada");
        }
        return tarefaMapper.toDTO(tarefa);
    }

    @Override
    @Transactional
    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO tarefaRequest, String usernameAutenticado) {
        Tarefa tarefa = tarefaRepository
                .findByIdComRelacionamentos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        if (!tarefa.getUsuario().getUsername().equals(usernameAutenticado)) {
            throw new AccessDeniedException("Acesso negado");
        }

        tarefa.setTitulo(tarefaRequest.titulo());
        tarefa.setDescricao(tarefaRequest.descricao());
        tarefa.setCategoria(buscarOuCriarCategoria(tarefaRequest.categoriaNome()));
        tarefa = tarefaRepository.save(tarefa);

        return tarefaMapper.toDTO(tarefa);
    }

    @Override
    @Transactional
    public TarefaResponseDTO marcarComoConcluida(Long id, String usernameAutenticado) {

        Tarefa tarefa = tarefaRepository
                .findByIdComRelacionamentos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        if (!tarefa.getUsuario().getUsername().equals(usernameAutenticado)) {
            throw new AccessDeniedException("Acesso negado");
        }

        tarefa.setConcluida(true);
        tarefa = tarefaRepository.save(tarefa);
        return tarefaMapper.toDTO(tarefa);
    }

    @Override
    @Transactional
    public void deletar(Long id, String usernameAutenticado) {
        Tarefa tarefa = tarefaRepository
                .findByIdComRelacionamentos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        if (!tarefa.getUsuario().getUsername().equals(usernameAutenticado)) {
            throw new AccessDeniedException("Acesso negado");
        }

        tarefaRepository.delete(tarefa);
    }
}
