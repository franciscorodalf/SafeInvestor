package es.franciscorodalf.saveinvestor.backend.model;

import java.util.Date;
import java.util.Objects;

public class tarea {
    public static final String ESTADO_INGRESO = "INGRESO";
    public static final String ESTADO_GASTO = "GASTO";

    private Integer id;
    private String concepto;
    private Double cantidad;
    private Date fecha;
    private String estado;
    private Integer usuarioId;

    public tarea() {
        this.cantidad = 0.0;
        this.fecha = null;
    }

    public tarea(String concepto, Double cantidad, Date fecha, String estado, Integer usuarioId) {
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.fecha = (fecha != null) ? fecha : new Date();
        this.estado = estado;
        this.usuarioId = usuarioId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad no puede ser cero ni negativa");
        }
        this.cantidad = cantidad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!ESTADO_INGRESO.equals(estado) && !ESTADO_GASTO.equals(estado)) {
            throw new IllegalArgumentException("Estado debe ser INGRESO o GASTO");
        }
        this.estado = estado;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
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
        return String.format("%s, %s, %s, %s", concepto, cantidad, fecha, estado);
    }
}
