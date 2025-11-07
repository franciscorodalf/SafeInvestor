package es.franciscorodalf.saveinvestor.backend.service;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de una simulación de metas que incluye los datos agregados y la
 * serie de puntos que componen la proyección.
 */
public class SimulacionMetaResultado {

    private final List<PuntoProyeccion> puntos;
    private final double saldoFinal;
    private final double totalAportado;
    private final double totalIntereses;
    private final Integer periodoAlcanceMeta;
    private final Double metaObjetivo;

    public SimulacionMetaResultado(List<PuntoProyeccion> puntos,
                                   double saldoFinal,
                                   double totalAportado,
                                   double totalIntereses,
                                   Integer periodoAlcanceMeta,
                                   Double metaObjetivo) {
        this.puntos = Collections.unmodifiableList(puntos);
        this.saldoFinal = saldoFinal;
        this.totalAportado = totalAportado;
        this.totalIntereses = totalIntereses;
        this.periodoAlcanceMeta = periodoAlcanceMeta;
        this.metaObjetivo = metaObjetivo;
    }

    public List<PuntoProyeccion> getPuntos() {
        return puntos;
    }

    public double getSaldoFinal() {
        return saldoFinal;
    }

    public double getTotalAportado() {
        return totalAportado;
    }

    public double getTotalIntereses() {
        return totalIntereses;
    }

    public Integer getPeriodoAlcanceMeta() {
        return periodoAlcanceMeta;
    }

    public Double getMetaObjetivo() {
        return metaObjetivo;
    }
}
