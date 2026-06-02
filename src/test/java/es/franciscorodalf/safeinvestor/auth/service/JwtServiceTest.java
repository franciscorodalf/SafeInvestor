package es.franciscorodalf.safeinvestor.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwt = new JwtService(
        "test-secret-with-at-least-32-bytes-padding-padding", 60
    );

    @Test
    void issuesAndParsesToken() {
        String token = jwt.issue("alice@example.com");
        assertEquals("alice@example.com", jwt.parseSubject(token));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwt.issue("alice@example.com");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertThrows(Exception.class, () -> jwt.parseSubject(tampered));
    }
}
