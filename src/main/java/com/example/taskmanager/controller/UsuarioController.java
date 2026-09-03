package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Dados do usuário autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    @Operation(summary = "Obtém o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado", content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido ou expirado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "sub do token não identifica usuário existente", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioResponseDTO response = usuarioService.obterUsuarioAtual(username);
        return ResponseEntity.ok(response);
    }
}
