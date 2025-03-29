package es.franciscorodalf.saveinvestor.backend.model;

import java.util.Date;
import java.util.Objects;

public class tarea {
    private String concepto;
    private categoria categoria;
    private double cantidad;
    private Date fecha;
    private String estado;

    public tarea() {
    }

    public tarea(String concepto, categoria categoria, double cantidad, Date fecha, String estado) {
        this.concepto = concepto;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.estado = estado;
    }

    public String getConcepto() {
        return this.concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public categoria getCategoria() {
        return this.categoria;
    }

    public void setCategoria(categoria categoria) {
        this.categoria = categoria;
    }

    public double getCantidad() {
        return this.cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Date getFecha() {
        return this.fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return this.estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public tarea concepto(String concepto) {
        setConcepto(concepto);
        return this;
    }

    public tarea categoria(categoria categoria) {
        setCategoria(categoria);
        return this;
    }

    public tarea cantidad(double cantidad) {
        setCantidad(cantidad);
        return this;
    }

    public tarea fecha(Date fecha) {
        setFecha(fecha);
        return this;
    }

    public tarea estado(String estado) {
        setEstado(estado);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof tarea)) {
            return false;
        }
        tarea tarea = (tarea) o;
        return Objects.equals(concepto, tarea.concepto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concepto);
    }

    @Override
    public String toString() {
        return getConcepto() + ", " + getCategoria() + ", " + getCantidad() + ", " + getFecha() + ", " + getEstado();
    }

}
