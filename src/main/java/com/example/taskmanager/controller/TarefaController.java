package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TarefaRequestDTO;
import com.example.taskmanager.dto.TarefaResponseDTO;
import com.example.taskmanager.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "CRUD e transições das tarefas do usuário autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class TarefaController {

    private final TarefaService tarefaService;

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    @Operation(summary = "Cria uma nova tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada", content = @Content(schema = @Schema(implementation = TarefaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido. A propriedade 'errors' mapeia campo para mensagem.", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TarefaResponseDTO> criar(@Valid @RequestBody TarefaRequestDTO request) {
        TarefaResponseDTO response = tarefaService.criar(request, getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todas as tarefas do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tarefas", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TarefaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<TarefaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(tarefaService.listarTodas(getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada", content = @Content(schema = @Schema(implementation = TarefaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente ou de outro usuário", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TarefaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador da tarefa", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id, getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma tarefa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada", content = @Content(schema = @Schema(implementation = TarefaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Corpo inválido. A propriedade 'errors' mapeia campo para mensagem.", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa de outro usuário", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TarefaResponseDTO> atualizar(
            @Parameter(description = "Identificador da tarefa", required = true) @PathVariable Long id,
            @Valid @RequestBody TarefaRequestDTO request) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request, getUsername()));
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Conclui uma tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa concluída", content = @Content(schema = @Schema(implementation = TarefaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa de outro usuário", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TarefaResponseDTO> concluir(
            @Parameter(description = "Identificador da tarefa", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.marcarComoConcluida(id, getUsername()));
    }

    @PatchMapping("/{id}/reabrir")
    @Operation(summary = "Reabre uma tarefa concluída")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa reaberta", content = @Content(schema = @Schema(implementation = TarefaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa de outro usuário", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TarefaResponseDTO> reabrir(
            @Parameter(description = "Identificador da tarefa", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.reabrir(id, getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa excluída"),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Tarefa de outro usuário", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa inexistente", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador da tarefa", required = true) @PathVariable Long id) {
        tarefaService.deletar(id, getUsername());
        return ResponseEntity.noContent().build();
    }
}
