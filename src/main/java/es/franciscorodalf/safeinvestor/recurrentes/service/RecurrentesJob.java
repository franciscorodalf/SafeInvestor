package es.franciscorodalf.safeinvestor.recurrentes.service;

import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import es.franciscorodalf.safeinvestor.recurrentes.domain.MovimientoRecurrente;
import es.franciscorodalf.safeinvestor.recurrentes.domain.MovimientoRecurrenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Materializa los movimientos recurrentes que ya están vencidos.
 *
 * Si una recurrencia se quedó atrasada varios periodos (por ejemplo, la app
 * estuvo offline una semana), el bucle interno mantiene creando movimientos
 * y avanzando la fecha hasta que proximaEjecucion vuelva al futuro. Esto
 * evita pérdidas silenciosas.
 *
 * Cron por defecto: cada día a las 03:00 (servidor). Configurable.
 */
@Component
public class RecurrentesJob {

    private static final Logger log = LoggerFactory.getLogger(RecurrentesJob.class);

    private final MovimientoRecurrenteRepository recurrentes;
    private final MovimientoService movimientos;

    public RecurrentesJob(MovimientoRecurrenteRepository recurrentes,
                          MovimientoService movimientos) {
        this.recurrentes = recurrentes;
        this.movimientos = movimientos;
    }

    @Scheduled(cron = "${app.recurrentes.cron:0 0 3 * * *}")
    @Transactional
    public void materializarRecurrenciasVencidas() {
        LocalDate hoy = LocalDate.now();
        List<MovimientoRecurrente> due = recurrentes.findDueOn(hoy);
        if (due.isEmpty()) {
            log.debug("Job recurrentes: nada que materializar");
            return;
        }
        int created = 0;
        for (MovimientoRecurrente r : due) {
            // Catch-up: si la recurrencia se quedó atrás varios periodos, crea uno
            // por cada uno hasta que la próxima ejecución vuelva al futuro
            while (!r.getProximaEjecucion().isAfter(hoy)) {
                try {
                    Long catId = r.getCategoria() != null ? r.getCategoria().getId() : null;
                    movimientos.create(r.getUsuario(), catId, r.getTipo(),
                            r.getImporte(), r.getDescripcion(), r.getProximaEjecucion());
                    created++;
                } catch (Exception e) {
                    log.warn("Error materializando recurrencia {} en {}: {}",
                            r.getId(), r.getProximaEjecucion(), e.getMessage());
                    break;
                }
                r.avanzarSiguienteEjecucion();
            }
        }
        log.info("Job recurrentes: {} movimientos creados desde {} plantillas", created, due.size());
    }
}
