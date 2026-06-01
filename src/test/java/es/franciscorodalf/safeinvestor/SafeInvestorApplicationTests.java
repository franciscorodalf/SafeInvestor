package es.franciscorodalf.safeinvestor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SafeInvestorApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring arranca contra el Postgres local
        // (definido en compose.yaml en localhost:5432). En CI, GitHub Actions
        // proporciona el mismo Postgres como service container.
    }
}
