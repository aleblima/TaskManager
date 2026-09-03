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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TarefaServiceImplTest {

    private final TarefaRepository tarefaRepository = mock(TarefaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final TransactionOperations categoriaTransactionOperations = mock(TransactionOperations.class);
    private final TarefaServiceImpl tarefaService = new TarefaServiceImpl(
            new TarefaMapper(),
            tarefaRepository,
            usuarioRepository,
            categoriaRepository,
            categoriaTransactionOperations);

    // --- criar ---

    @Test
    void criar_tarefa_retorna_dto() {
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha");
        Categoria categoria = new Categoria(1L, "Pessoal");
        TarefaRequestDTO request = new TarefaRequestDTO("Tarefa", "Descricao", "Pessoal");

        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findByNomeNormalizado("pessoal")).thenReturn(Optional.of(categoria));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TarefaResponseDTO resposta = tarefaService.criar(request, "ana12345");

        assertNotNull(resposta);
        assertEquals("Tarefa", resposta.titulo());
        assertFalse(resposta.concluida());
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    void criar_tarefa_usuario_inexistente_lanca_excecao() {
        TarefaRequestDTO request = new TarefaRequestDTO("Tarefa", "Descricao", "Pessoal");
        when(usuarioRepository.findByUsername("naoexiste")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.criar(request, "naoexiste"));

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void criar_tarefa_com_categoria_nova_persiste_chave_normalizada() {
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha");
        Categoria categoria = new Categoria(2L, "Nova");
        TarefaRequestDTO request = new TarefaRequestDTO("Tarefa", "Descricao", "Nova");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findByNomeNormalizado("nova")).thenReturn(Optional.empty());
        executarTransacaoDeCategoria();
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoria);
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        tarefaService.criar(new TarefaRequestDTO("Tarefa", "Descricao", " Nova "), "ana12345");

        verify(categoriaRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(categoriaNova ->
                categoriaNova.getNome().equals(" Nova ")
                        && categoriaNova.getNomeNormalizado().equals("nova")));
    }

    @Test
    void criar_tarefa_colisao_de_categoria_reutiliza_vencedora() {
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha");
        Categoria categoriaExistente = new Categoria(2L, "Nova");
        TarefaRequestDTO request = new TarefaRequestDTO("Tarefa", "Descricao", "NOVA");
        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findByNomeNormalizado("nova"))
                .thenReturn(Optional.empty(), Optional.of(categoriaExistente));
        executarTransacaoDeCategoria();
        when(categoriaRepository.saveAndFlush(any(Categoria.class)))
                .thenThrow(new DataIntegrityViolationException("uk_categoria_nome_normalizado"));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tarefaService.criar(request, "ana12345");

        verify(tarefaRepository).save(org.mockito.ArgumentMatchers.argThat(tarefa ->
                tarefa.getCategoria() == categoriaExistente));
    }

    // --- listarTodas ---

    @Test
    void listarTodas_retorna_lista_de_dtos() {
        Usuario usuario = new Usuario(1L, "Ana", "ana12345", "senha");
        Categoria categoria = new Categoria(1L, "Pessoal");
        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa");
        tarefa.setConcluida(false);
        tarefa.setUsuario(usuario);
        tarefa.setCategoria(categoria);

        when(usuarioRepository.findByUsername("ana12345")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.findByUsuarioId(1L)).thenReturn(List.of(tarefa));

        List<TarefaResponseDTO> resposta = tarefaService.listarTodas("ana12345");

        assertNotNull(resposta);
        assertEquals(1, resposta.size());
        assertEquals("Tarefa", resposta.get(0).titulo());
    }

    @Test
    void listarTodas_usuario_inexistente_lanca_excecao() {
        when(usuarioRepository.findByUsername("naoexiste")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.listarTodas("naoexiste"));
    }

    // --- buscarPorId ---

    @Test
    void buscarPorId_tarefa_do_dono_retorna_dto() {
        Tarefa tarefa = tarefaDoUsuario("ana12345", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        TarefaResponseDTO resposta = tarefaService.buscarPorId(1L, "ana12345");

        assertNotNull(resposta);
        assertEquals(1L, resposta.id());
        assertEquals("Tarefa", resposta.titulo());
    }

    @Test
    void buscarPorId_tarefa_inexistente_lanca_excecao() {
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.buscarPorId(1L, "ana12345"));
    }

    @Test
    void buscarPorId_tarefa_de_outro_usuario_lanca_excecao() {
        Tarefa tarefa = tarefaDoUsuario("bruno123", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.buscarPorId(1L, "ana12345"));
    }

    // --- atualizar ---

    @Test
    void atualizar_tarefa_do_dono_retorna_dto() {
        Tarefa tarefa = tarefaDoUsuario("ana12345", false);
        Categoria categoria = new Categoria(1L, "Pessoal");
        TarefaRequestDTO request = new TarefaRequestDTO("Novo Titulo", "Nova Desc", "Pessoal");

        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));
        when(categoriaRepository.findByNomeNormalizado("pessoal")).thenReturn(Optional.of(categoria));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponseDTO resposta = tarefaService.atualizar(1L, request, "ana12345");

        assertNotNull(resposta);
        assertEquals("Novo Titulo", resposta.titulo());
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    void atualizar_tarefa_inexistente_lanca_excecao() {
        TarefaRequestDTO request = new TarefaRequestDTO("Titulo", "Desc", "Pessoal");
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.atualizar(1L, request, "ana12345"));

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void atualizar_tarefa_de_outro_usuario_lanca_access_denied() {
        Tarefa tarefa = tarefaDoUsuario("bruno123", false);
        TarefaRequestDTO request = new TarefaRequestDTO("Titulo", "Desc", "Pessoal");
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(AccessDeniedException.class, () -> tarefaService.atualizar(1L, request, "ana12345"));

        verify(tarefaRepository, never()).save(any());
    }

    // --- marcarComoConcluida ---

    @Test
    void marcarComoConcluida_tarefa_do_dono_retorna_concluida() {
        Tarefa tarefa = tarefaDoUsuario("ana12345", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(tarefa)).thenReturn(tarefa);

        TarefaResponseDTO resposta = tarefaService.marcarComoConcluida(1L, "ana12345");

        assertTrue(resposta.concluida());
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    void marcarComoConcluida_tarefa_ja_concluida_permanece_concluida() {
        Tarefa tarefa = tarefaDoUsuario("ana12345", true);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(tarefa)).thenReturn(tarefa);

        assertTrue(tarefaService.marcarComoConcluida(1L, "ana12345").concluida());
    }

    @Test
    void marcarComoConcluida_tarefa_inexistente_lanca_excecao() {
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.marcarComoConcluida(1L, "ana12345"));

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void marcarComoConcluida_tarefa_de_outro_usuario_lanca_access_denied() {
        Tarefa tarefa = tarefaDoUsuario("bruno123", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(AccessDeniedException.class, () -> tarefaService.marcarComoConcluida(1L, "ana12345"));

        assertFalse(tarefa.getConcluida());
        verify(tarefaRepository, never()).save(any());
    }

    // --- deletar ---

    @Test
    void deletar_tarefa_do_dono_remove() {
        Tarefa tarefa = tarefaDoUsuario("ana12345", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        tarefaService.deletar(1L, "ana12345");

        verify(tarefaRepository).delete(tarefa);
    }

    @Test
    void deletar_tarefa_inexistente_lanca_excecao() {
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.deletar(1L, "ana12345"));

        verify(tarefaRepository, never()).delete(any());
    }

    @Test
    void deletar_tarefa_de_outro_usuario_lanca_access_denied() {
        Tarefa tarefa = tarefaDoUsuario("bruno123", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(AccessDeniedException.class, () -> tarefaService.deletar(1L, "ana12345"));

        verify(tarefaRepository, never()).delete(any());
    }

    // --- reabrir ---

    @Test
    void reabrir_define_tarefa_do_dono_como_pendente() {
        Tarefa tarefa = tarefaDoUsuario("ana", true);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(tarefa)).thenReturn(tarefa);

        TarefaResponseDTO resposta = tarefaService.reabrir(1L, "ana");

        assertFalse(resposta.concluida());
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    void reabrir_tarefa_ja_pendente_permanece_pendente() {
        Tarefa tarefa = tarefaDoUsuario("ana", false);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(tarefa)).thenReturn(tarefa);

        assertFalse(tarefaService.reabrir(1L, "ana").concluida());
    }

    @Test
    void reabrir_tarefa_inexistente_lanca_resource_not_found() {
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.reabrir(1L, "ana"));

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void reabrir_tarefa_de_outro_usuario_lanca_access_denied() {
        Tarefa tarefa = tarefaDoUsuario("bruno", true);
        when(tarefaRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(AccessDeniedException.class, () -> tarefaService.reabrir(1L, "ana"));

        assertTrue(tarefa.getConcluida());
        verify(tarefaRepository, never()).save(any());
    }

    private Tarefa tarefaDoUsuario(String username, boolean concluida) {
        Usuario usuario = new Usuario(1L, "Usuário", username, "senha");
        Categoria categoria = new Categoria(1L, "Pessoal");
        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa");
        tarefa.setConcluida(concluida);
        tarefa.setUsuario(usuario);
        tarefa.setCategoria(categoria);
        return tarefa;
    }

    @SuppressWarnings("unchecked")
    private void executarTransacaoDeCategoria() {
        when(categoriaTransactionOperations.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<Categoria>) invocation.getArgument(0))
                        .doInTransaction(mock(TransactionStatus.class)));
    }
}
