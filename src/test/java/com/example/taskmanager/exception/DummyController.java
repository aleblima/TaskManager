package com.example.taskmanager.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dummy")
class DummyController {
    @GetMapping("/not-found")
    void notFound() { throw new ResourceNotFoundException("Recurso não encontrado"); }

    @GetMapping("/access-denied")
    void accessDenied() { throw new AccessDeniedException("Acesso negado"); }

    @GetMapping("/unauthorized")
    void unauthorized() { throw new CredenciaisInvalidasException("Credenciais inválidas"); }

    @GetMapping("/conflict")
    void conflict() { throw new RegraDeNegocioException("Username em uso"); }
}
