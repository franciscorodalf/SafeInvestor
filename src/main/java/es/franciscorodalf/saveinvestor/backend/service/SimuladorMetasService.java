package es.franciscorodalf.saveinvestor.backend.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de calcular proyecciones de ahorro para metas
 * financieras considerando tasas de interés compuestas, aportaciones
 * periódicas y horizonte temporal.
 */
public class SimuladorMetasService {

    /**
     * Genera una proyección mensual de ahorro.
     *
     * @param montoInicial        capital inicial disponible
     * @param aportacionPeriodica ahorro que se aportará cada mes
     * @param tasaInteresAnual    tasa de interés anual en formato decimal (0.05 = 5%)
     * @param horizonteMeses      número de meses a simular
     * @param metaObjetivo        monto objetivo opcional para calcular cuándo se alcanza
     * @return resultado con la información acumulada de la simulación
     */
    public SimulacionMetaResultado simular(double montoInicial,
                                           double aportacionPeriodica,
                                           double tasaInteresAnual,
                                           int horizonteMeses,
                                           Double metaObjetivo) {
        if (montoInicial < 0 || aportacionPeriodica < 0) {
            throw new IllegalArgumentException("Los montos no pueden ser negativos");
        }
        if (tasaInteresAnual < -0.999) {
            throw new IllegalArgumentException("La tasa anual no puede ser menor a -99.9%");
        }
        if (horizonteMeses <= 0) {
            throw new IllegalArgumentException("El horizonte debe ser mayor a cero");
        }

        double tasaMensual = calcularTasaMensual(tasaInteresAnual);
        double saldo = montoInicial;
        double totalAportado = montoInicial;
        double interesesAcumulados = 0;
        Integer periodoAlcance = null;

        List<PuntoProyeccion> puntos = new ArrayList<>();
        puntos.add(new PuntoProyeccion(0, redondear(saldo), redondear(totalAportado), redondear(interesesAcumulados)));

        for (int mes = 1; mes <= horizonteMeses; mes++) {
            saldo += aportacionPeriodica;
            totalAportado += aportacionPeriodica;

            double interesMes = saldo * tasaMensual;
            interesesAcumulados += interesMes;
            saldo += interesMes;

            if (metaObjetivo != null && periodoAlcance == null && saldo >= metaObjetivo) {
                periodoAlcance = mes;
            }

            puntos.add(new PuntoProyeccion(mes, redondear(saldo), redondear(totalAportado), redondear(interesesAcumulados)));
        }

        return new SimulacionMetaResultado(puntos,
                redondear(saldo),
                redondear(totalAportado),
                redondear(interesesAcumulados),
                periodoAlcance,
                metaObjetivo);
    }

    /**
     * Calcula la aportación periódica necesaria para alcanzar un objetivo.
     *
     * @param montoInicial     capital inicial disponible
     * @param tasaInteresAnual tasa de interés anual (decimal)
     * @param horizonteMeses   meses en los que se quiere alcanzar la meta
     * @param metaObjetivo     monto objetivo
     * @return aportación mensual recomendada
     */
    public double calcularAportacionNecesaria(double montoInicial,
                                              double tasaInteresAnual,
                                              int horizonteMeses,
                                              double metaObjetivo) {
        if (metaObjetivo <= 0) {
            throw new IllegalArgumentException("La meta debe ser mayor a cero");
        }
        if (horizonteMeses <= 0) {
            throw new IllegalArgumentException("El horizonte debe ser mayor a cero");
        }

        double tasaMensual = calcularTasaMensual(tasaInteresAnual);
        double factorCapitalizado = Math.pow(1 + tasaMensual, horizonteMeses);
        double valorObjetivo = metaObjetivo - montoInicial * factorCapitalizado;

        if (Math.abs(tasaMensual) < 1e-9) {
            return redondear(valorObjetivo / horizonteMeses);
        }

        double aportacion = valorObjetivo * tasaMensual / (factorCapitalizado - 1);
        return redondear(aportacion);
    }

    private double calcularTasaMensual(double tasaInteresAnual) {
        return Math.pow(1 + tasaInteresAnual, 1.0 / 12) - 1;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
