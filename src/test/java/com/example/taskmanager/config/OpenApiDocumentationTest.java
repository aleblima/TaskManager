package com.example.taskmanager.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentacao_openapi_expoe_contrato_publico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TaskManager API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/auth/registro'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/usuarios/me'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/tarefas'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['400'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['401'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/tarefas/{id}'].get.responses['404'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/tarefas/{id}'].get.responses['403']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/tarefas/{id}'].put.responses['400'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/tarefas/{id}'].put.responses['401'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/tarefas/{id}'].put.responses['403'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/v1/tarefas/{id}'].put.responses['404'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"));
    }

    @Test
    void swagger_ui_publica_fica_disponivel_sem_jwt() throws Exception {
        mockMvc.perform(get("/docs"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
