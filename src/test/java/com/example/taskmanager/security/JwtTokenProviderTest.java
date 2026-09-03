package com.example.taskmanager.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(
            "tF7uW2pQ9mZx4R8vL1cD5kG0sH3jN6bY", 10800000);

    @Test
    void gerarToken_tem_subject_e_expiracao_de_tres_horas() {
        String token = provider.gerarToken("ana12345");
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "tF7uW2pQ9mZx4R8vL1cD5kG0sH3jN6bY".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();

        assertEquals("ana12345", claims.getSubject());
        assertEquals(10_800_000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void validarToken_token_valido_retorna_true() {
        String token = provider.gerarToken("ana12345");
        assertTrue(provider.validarToken(token));
    }

    @Test
    void validarToken_token_invalido_retorna_false() {
        assertFalse(provider.validarToken("token-inexistente"));
    }

    @Test
    void getUsernameFromToken_token_valido_retorna_username() {
        String token = provider.gerarToken("ana12345");
        assertEquals("ana12345", provider.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromToken_token_invalido_lanca_excecao() {
        assertThrows(JwtException.class,
                () -> provider.getUsernameFromToken("token-invalido"));
    }
}
