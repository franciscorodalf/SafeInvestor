package es.franciscorodalf.saveinvestor.backend.model;

public class estadistica {
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

    public double getTotalIngreso() {
        return this.totalIngreso;
    }

    public void setTotalIngreso(double totalIngreso) {
        this.totalIngreso = totalIngreso;
    }

    public double getTotalGasto() {
        return this.totalGasto;
    }

    public void setTotalGasto(double totalGasto) {
        this.totalGasto = totalGasto;
    }

}
