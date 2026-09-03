package com.example.taskmanager.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class AuthenticationProblemDetailEntryPointTest {

    @Test
    void commence_resposta_401_preserva_acentos_em_utf8() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthenticationProblemDetailEntryPoint().commence(
                new MockHttpServletRequest(), response,
                new BadCredentialsException("Token inválido"));

        assertEquals(StandardCharsets.UTF_8.name(), response.getCharacterEncoding());
        assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("Não autorizado"));
    }
}
