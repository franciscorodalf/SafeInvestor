package es.franciscorodalf.safeinvestor.objetivos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.email.EmailService;
import es.franciscorodalf.safeinvestor.objetivos.domain.Objetivo;
import es.franciscorodalf.safeinvestor.objetivos.domain.ObjetivoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ObjetivoService {

    private final ObjetivoRepository objetivos;
    private final EmailService emailService;
    private final MessageSource messages;
    private final String baseUrl;

    public ObjetivoService(ObjetivoRepository objetivos,
                           EmailService emailService,
                           MessageSource messages,
                           @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.objetivos = objetivos;
        this.emailService = emailService;
        this.messages = messages;
        this.baseUrl = baseUrl;
    }

    public List<Objetivo> findAll(Usuario usuario) {
        return objetivos.findAllOrdered(usuario);
    }

    public Objetivo get(Usuario usuario, Long id) {
        return objetivos.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new ObjetivoNotFoundException(id));
    }

    @Transactional
    public Objetivo create(Usuario usuario, String nombre, BigDecimal importeObjetivo,
                           LocalDate fechaLimite, String color) {
        return objetivos.save(new Objetivo(usuario, nombre, importeObjetivo, fechaLimite, color));
    }

    @Transactional
    public Objetivo update(Usuario usuario, Long id, String nombre, BigDecimal importeObjetivo,
                           LocalDate fechaLimite, String color) {
        Objetivo o = get(usuario, id);
        o.setNombre(nombre);
        o.setImporteObjetivo(importeObjetivo);
        o.setFechaLimite(fechaLimite);
        if (color != null) o.setColor(color);
        // Si el objetivo nuevo es mayor, ya no está completado
        if (o.getCompletadoAt() != null && o.getImporteAhorrado().compareTo(importeObjetivo) < 0) {
            o.setCompletadoAt(null);
        }
        return o;
    }

    @Transactional
    public Objetivo aportar(Usuario usuario, Long id, BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe a aportar debe ser positivo");
        }
        Objetivo o = get(usuario, id);
        boolean wasCompleted = o.getCompletadoAt() != null;
        o.setImporteAhorrado(o.getImporteAhorrado().add(importe));
        boolean justReached = !wasCompleted
                && o.getImporteAhorrado().compareTo(o.getImporteObjetivo()) >= 0;
        if (justReached) {
            o.setCompletadoAt(OffsetDateTime.now());
            sendObjetivoCompletadoEmail(usuario, o);
        }
        return o;
    }

    private void sendObjetivoCompletadoEmail(Usuario usuario, Objetivo objetivo) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            String subject = messages.getMessage("email.objetivo.subject", null, locale);
            DecimalFormatSymbols sym = new DecimalFormatSymbols(locale);
            DecimalFormat fmt = new DecimalFormat("#,##0.00", sym);
            emailService.send(usuario.getEmail(), subject, "objetivo-completado", Map.of(
                    "nombre",         usuario.getNombre(),
                    "objetivoNombre", objetivo.getNombre(),
                    "importe",        fmt.format(objetivo.getImporteObjetivo()),
                    "baseUrl",        baseUrl
            ), locale);
        } catch (Exception e) {
            // No queremos romper la transacción si el email falla
            org.slf4j.LoggerFactory.getLogger(ObjetivoService.class)
                    .warn("No se pudo enviar email de objetivo completado: {}", e.getMessage());
        }
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        objetivos.delete(get(usuario, id));
    }

    public static class ObjetivoNotFoundException extends RuntimeException {
        public ObjetivoNotFoundException(Long id) { super("Objetivo no encontrado: " + id); }
    }
}
