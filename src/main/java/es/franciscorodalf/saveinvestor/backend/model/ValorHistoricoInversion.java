package es.franciscorodalf.saveinvestor.backend.model;

import java.time.LocalDate;

public class ValorHistoricoInversion {
    private final LocalDate fecha;
    private final double valorTotal;

    public ValorHistoricoInversion(LocalDate fecha, double valorTotal) {
        this.fecha = fecha;
        this.valorTotal = valorTotal;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getValorTotal() {
        return valorTotal;
    }
}
