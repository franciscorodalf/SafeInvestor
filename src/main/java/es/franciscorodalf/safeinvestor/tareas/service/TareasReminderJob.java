package es.franciscorodalf.safeinvestor.tareas.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.email.EmailService;
import es.franciscorodalf.safeinvestor.tareas.domain.Tarea;
import es.franciscorodalf.safeinvestor.tareas.domain.TareaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Job que cada día (cron configurable via {@code app.email.reminders.cron})
 * recorre todas las tareas vencidas y agrupa por usuario para enviarle un
 * único email con su lista de tareas pendientes pasadas de fecha.
 */
@Component
public class TareasReminderJob {

    private static final Logger log = LoggerFactory.getLogger(TareasReminderJob.class);

    private final TareaRepository tareas;
    private final EmailService emailService;
    private final MessageSource messages;
    private final String baseUrl;

    public TareasReminderJob(TareaRepository tareas,
                             EmailService emailService,
                             MessageSource messages,
                             @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.tareas = tareas;
        this.emailService = emailService;
        this.messages = messages;
        this.baseUrl = baseUrl;
    }

    @Scheduled(cron = "${app.email.reminders.cron:0 0 9 * * *}")
    public void enviarRecordatoriosDeTareasVencidas() {
        List<Tarea> vencidas = tareas.findAllVencidas(LocalDate.now());
        if (vencidas.isEmpty()) {
            log.debug("Job recordatorios: sin tareas vencidas hoy");
            return;
        }

        // Agrupa por usuario para mandar un único email por persona
        Map<Usuario, List<Tarea>> porUsuario = vencidas.stream()
                .collect(Collectors.groupingBy(Tarea::getUsuario));

        log.info("Job recordatorios: {} tareas vencidas en {} usuarios", vencidas.size(), porUsuario.size());

        porUsuario.forEach((usuario, lista) -> {
            try {
                // Usamos el locale por defecto del MessageSource (la cookie del usuario
                // no está disponible aquí porque el job corre fuera de un request)
                Locale locale = Locale.forLanguageTag("es");
                String subject = messages.getMessage(
                        "email.tareas.subject",
                        new Object[]{ lista.size() },
                        locale);
                emailService.send(usuario.getEmail(), subject, "tareas-vencidas", Map.of(
                        "nombre",  usuario.getNombre(),
                        "tareas",  lista,
                        "baseUrl", baseUrl
                ), locale);
            } catch (Exception e) {
                log.warn("Error enviando recordatorio a {}: {}", usuario.getEmail(), e.getMessage());
            }
        });
    }
}
