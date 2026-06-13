package es.franciscorodalf.safeinvestor.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio centralizado de envío de emails.
 *
 * Renderiza una plantilla Thymeleaf de {@code templates/email/*.html} y la
 * envía como cuerpo HTML. Si {@code app.email.enabled=false} (default) o no
 * hay {@link JavaMailSender} disponible, el email solo se loguea — útil en
 * dev/CI sin credenciales SMTP.
 *
 * Convenciones:
 * <ul>
 *   <li>Las variables disponibles en la plantilla son las pasadas en {@code vars}
 *       más el modelo i18n (#{key}) y la {@code locale} activa.</li>
 *   <li>El subject se renderiza pasándolo tal cual; localizable desde el caller.</li>
 *   <li>El remitente sale de {@code app.email.from}.</li>
 * </ul>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SpringTemplateEngine templateEngine;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String from;

    public EmailService(SpringTemplateEngine templateEngine,
                        ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${app.email.enabled:false}") boolean enabled,
                        @Value("${app.email.from:SafeInvestor <no-reply@safeinvestor.app>}") String from) {
        this.templateEngine = templateEngine;
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.from = from;
    }

    /**
     * Renderiza la plantilla {@code email/<template>.html} y la envía.
     *
     * @param to       email destinatario
     * @param subject  asunto ya localizado
     * @param template nombre de plantilla bajo templates/email/ (sin .html)
     * @param vars     variables Thymeleaf
     */
    public void send(String to, String subject, String template, Map<String, Object> vars) {
        send(to, subject, template, vars, LocaleContextHolder.getLocale());
    }

    public void send(String to, String subject, String template, Map<String, Object> vars, Locale locale) {
        if (to == null || to.isBlank()) {
            log.warn("Skip envío email: destinatario vacío (template={})", template);
            return;
        }

        Context ctx = new Context(locale != null ? locale : Locale.getDefault());
        if (vars != null) vars.forEach(ctx::setVariable);
        String html;
        try {
            html = templateEngine.process("email/" + template, ctx);
        } catch (Exception e) {
            log.error("Error renderizando plantilla email/{}: {}", template, e.getMessage(), e);
            return;
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (!enabled || sender == null) {
            log.info("📧 [DEV] Email a {} | asunto: {} | plantilla: {}\n{}", to, subject, template, html);
            return;
        }

        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(msg);
            log.info("Email enviado a {} ({})", to, template);
        } catch (Exception e) {
            log.error("Error enviando email a {} ({}): {}", to, template, e.getMessage(), e);
        }
    }
}
