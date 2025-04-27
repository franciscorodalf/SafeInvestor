package es.franciscorodalf.saveinvestor.backend.model;

import java.util.Date;
import java.util.Objects;

public class tarea {
    public static final String ESTADO_INGRESO = "INGRESO";
    public static final String ESTADO_GASTO = "GASTO";

    private Integer id;
    private Integer usuarioId;
    private String concepto;
    private double cantidad;
    private Date fecha;
    private String estado;

    public tarea() {
    }

    public tarea(String concepto, double cantidad, Date fecha, String estado, Integer usuarioId) {
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.estado = estado;
        this.usuarioId = usuarioId;
    }

    public String getConcepto() {
        return this.concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getCantidad() {
        return this.cantidad;
    }

    public void setCantidad(double cantidad) {
        if (cantidad == 0) {
            throw new IllegalArgumentException("La cantidad no puede ser cero");
        }
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
        if (!estado.equals(ESTADO_INGRESO) && !estado.equals(ESTADO_GASTO)) {
            throw new IllegalArgumentException("Estado debe ser INGRESO o GASTO");
        }
        this.estado = estado;
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

    public tarea concepto(String concepto) {
        setConcepto(concepto);
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
        return getConcepto() + ", " + getCantidad() + ", " + getFecha() + ", " + getEstado();
    }

}
