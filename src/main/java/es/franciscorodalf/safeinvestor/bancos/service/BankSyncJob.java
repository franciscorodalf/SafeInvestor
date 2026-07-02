package es.franciscorodalf.safeinvestor.bancos.service;

import es.franciscorodalf.safeinvestor.bancos.client.GoCardlessClient;
import es.franciscorodalf.safeinvestor.bancos.domain.BankConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Sincronización periódica de todas las conexiones bancarias activas.
 * Cron por defecto: cada día a las 04:00 servidor. Configurable via
 * {@code gocardless.sync-cron}.
 *
 * Si una conexión ha expirado (>90 días desde linkedAt), se marca EXPIRED
 * y se salta — el usuario debe reautorizar.
 */
@Component
public class BankSyncJob {

    private static final Logger log = LoggerFactory.getLogger(BankSyncJob.class);

    private final BancoService bancoService;
    private final GoCardlessClient client;

    public BankSyncJob(BancoService bancoService, GoCardlessClient client) {
        this.bancoService = bancoService;
        this.client = client;
    }

    @Scheduled(cron = "${gocardless.sync-cron:0 0 4 * * *}")
    public void syncAll() {
        if (!client.isConfigured()) {
            log.debug("Sync bancario: GoCardless no configurado, salto");
            return;
        }
        var linked = bancoService.findAllLinked();
        if (linked.isEmpty()) return;
        log.info("Sync bancario: {} conexiones activas", linked.size());
        OffsetDateTime now = OffsetDateTime.now();
        int okConns = 0;
        for (BankConnection c : linked) {
            try {
                if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(now)) {
                    c.markExpired();
                    continue;
                }
                var r = bancoService.sync(c.getUsuario(), c.getId());
                log.info("Sync {} → {} creados, {} duplicados, {} errores",
                        c.getInstitutionName(), r.created(), r.skipped(), r.errored());
                okConns++;
            } catch (Exception e) {
                log.warn("Error sincronizando conexión {}: {}", c.getId(), e.getMessage());
            }
        }
        log.info("Sync bancario completado: {} conexiones procesadas", okConns);
    }
}
