package es.franciscorodalf.saveinvestor.backend.model;

public class estadistica {
    private Integer id;
    private Integer usuarioId;
    private double totalIngreso;
    private double totalGasto;

    public estadistica() {
    }

    public double calcularBalance() {
        return totalIngreso - totalGasto;
    }

    public String generarResumen() {
        return "Ingresos: " + totalIngreso + ", Gastos: " + totalGasto;
    }

    public estadistica(double totalIngreso, double totalGasto) {
        this.totalIngreso = totalIngreso;
        this.totalGasto = totalGasto;
    }

    public estadistica(double totalIngreso, double totalGasto, Integer usuarioId) {
        this.totalIngreso = totalIngreso;
        this.totalGasto = totalGasto;
        this.usuarioId = usuarioId;
    }

    public double getTotalIngreso() {
        return this.totalIngreso;
    }

    public void setTotalIngreso(double totalIngreso) {
        if (totalIngreso < 0) {
            throw new IllegalArgumentException("El total de ingresos no puede ser negativo");
        }
        this.totalIngreso = totalIngreso;
    }

    public double getTotalGasto() {
        return this.totalGasto;
    }

    public void setTotalGasto(double totalGasto) {
        if (totalGasto < 0) {
            throw new IllegalArgumentException("El total de gastos no puede ser negativo");
        }
        this.totalGasto = totalGasto;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return this.usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return "Estadistica{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", totalIngreso=" + totalIngreso +
               ", totalGasto=" + totalGasto +
               '}';
    }
}
