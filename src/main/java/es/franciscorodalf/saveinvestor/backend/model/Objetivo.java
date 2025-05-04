package es.franciscorodalf.saveinvestor.backend.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

/**
 * Representa un objetivo financiero del usuario
 */
public class Objetivo {

    private Integer id;
    private String descripcion;
    private Double cantidadObjetivo;
    private Double cantidadActual;
    private Date fechaCreacion;
    private Date fechaObjetivo;
    private boolean completado;
    private Integer usuarioId;

    public Objetivo() {
        this.fechaCreacion = new Date();
        this.cantidadActual = 0.0;
        this.completado = false;
    }

    public Objetivo(String descripcion, Double cantidadObjetivo, Integer usuarioId) {
        this();
        this.descripcion = descripcion;
        this.cantidadObjetivo = cantidadObjetivo;
        this.usuarioId = usuarioId;
    }

    /**
     * Calcula el porcentaje de progreso hacia el objetivo
     * 
     * @return Porcentaje de progreso (0-100)
     */
    public double calcularPorcentaje() {
        if (cantidadObjetivo <= 0) {
            return 0;
        }
        return Math.min(100, (cantidadActual / cantidadObjetivo) * 100);
    }

    /**
     * Calcula cuánto falta para completar el objetivo
     * 
     * @return Cantidad que falta para completar el objetivo
     */
    public double calcularCantidadFaltante() {
        if (cantidadObjetivo <= cantidadActual) {
            return 0;
        }
        return cantidadObjetivo - cantidadActual;
    }

    /**
     * Calcula el número de días restantes hasta la fecha objetivo
     * 
     * @return Número de días restantes, 0 si está completado, -1 si no tiene fecha
     */
    public long calcularDiasRestantes() {
        if (completado || fechaObjetivo == null) {
            return -1;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = fechaObjetivo.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return Math.max(0, ChronoUnit.DAYS.between(hoy, fechaLimite));
    }

    /**
     * Verifica si el objetivo está completado
     * 
     * @return true si la cantidad actual >= cantidad objetivo
     */
    public boolean verificarCompletado() {
        return cantidadActual >= cantidadObjetivo;
    }

    /**
     * Verifica si el objetivo está vencido pero no completado
     * 
     * @return true si está vencido y no completado
     */
    public boolean estaVencido() {
        if (fechaObjetivo == null || completado) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = fechaObjetivo.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return hoy.isAfter(fechaLimite);
    }

    /**
     * Actualiza la cantidad actual y el estado de completado
     * 
     * @param nuevaCantidad la nueva cantidad acumulada
     */
    public void actualizarCantidad(Double nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.cantidadActual = nuevaCantidad;
        this.completado = verificarCompletado();
    }

    /**
     * Añade una cantidad al ahorro actual
     * 
     * @param cantidad Cantidad a añadir
     * @return true si el objetivo se completa con esta contribución
     */
    public boolean contribuir(Double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La contribución debe ser positiva");
        }

        this.cantidadActual += cantidad;
        boolean nuevoCompletado = verificarCompletado();
        this.completado = nuevoCompletado;

        return nuevoCompletado && !completado;
    }

    // Getter para monto (necesario para ObjetivosController)
    public Double getMonto() {
        return cantidadObjetivo;
    }

    // Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getCantidadObjetivo() {
        return cantidadObjetivo;
    }

    public void setCantidadObjetivo(Double cantidadObjetivo) {
        if (cantidadObjetivo <= 0) {
            throw new IllegalArgumentException("La cantidad objetivo debe ser mayor que cero");
        }
        this.cantidadObjetivo = cantidadObjetivo;
    }

    public Double getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(Double cantidadActual) {
        if (cantidadActual < 0) {
            throw new IllegalArgumentException("La cantidad actual no puede ser negativa");
        }
        this.cantidadActual = cantidadActual;
        this.completado = verificarCompletado();
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaObjetivo() {
        return fechaObjetivo;
    }

    public void setFechaObjetivo(Date fechaObjetivo) {
        this.fechaObjetivo = fechaObjetivo;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Objetivo objetivo = (Objetivo) o;
        return Objects.equals(id, objetivo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Objetivo{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", cantidadObjetivo=" + cantidadObjetivo +
                ", cantidadActual=" + cantidadActual +
                ", progreso=" + calcularPorcentaje() + "%" +
                ", completado=" + completado +
                '}';
    }
}
