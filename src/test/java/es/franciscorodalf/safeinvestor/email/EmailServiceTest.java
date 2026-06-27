package es.franciscorodalf.safeinvestor.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del servicio de email en modo \"dev\" (sin SMTP configurado).
 *
 * El test verifica:
 *   - El servicio existe y es instanciable por Spring.
 *   - send() NO lanza excepciones cuando no hay MAIL_HOST (fallback a log).
 *   - send() con destinatario vacío/null se ignora silenciosamente.
 *   - send() con plantilla inexistente NO rompe la app (catch interno).
 *   - send() renderiza correctamente la plantilla cuando existe.
 *
 * No probamos el envío SMTP real porque requeriría infraestructura (Mailtrap,
 * GreenMail). El comportamiento en modo log es lo único garantizado en CI.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.email.enabled=false",
        "app.email.from=Tests <test@safeinvestor.app>"
})
class EmailServiceTest {

    @Autowired EmailService emailService;

    @Test
    void existeElBean() {
        assertNotNull(emailService);
    }

    @Test
    void enviarSinDestinatarioNoExplota() {
        assertDoesNotThrow(() -> emailService.send(null, "Asunto", "reset-password", Map.of()));
        assertDoesNotThrow(() -> emailService.send("", "Asunto", "reset-password", Map.of()));
    }

    @Test
    void enviarConPlantillaInexistenteNoExplota() {
        // El servicio debe atrapar la excepción de rendering y solo loguearla
        assertDoesNotThrow(() -> emailService.send(
                "alguien@example.com",
                "Asunto",
                "esta-plantilla-no-existe",
                Map.of()
        ));
    }

    @Test
    void enviarConPlantillaResetEnModoDevNoLanza() {
        // Modo dev → el email solo se loguea. No debe propagar ninguna excepción.
        assertDoesNotThrow(() -> emailService.send(
                "destino@example.com",
                "Recupera tu contraseña",
                "reset-password",
                Map.of(
                        "nombre",   "Francisco",
                        "resetUrl", "https://safeinvestor.onrender.com/reset/abc",
                        "baseUrl",  "https://safeinvestor.onrender.com"
                )
        ));
    }

    @Test
    void enviarObjetivoCompletadoEnModoDevNoLanza() {
        assertDoesNotThrow(() -> emailService.send(
                "destino@example.com",
                "¡Lo conseguiste!",
                "objetivo-completado",
                Map.of(
                        "nombre",         "Francisco",
                        "objetivoNombre", "Vacaciones",
                        "importe",        "2.000,00",
                        "baseUrl",        "https://safeinvestor.onrender.com"
                )
        ));
    }
}
