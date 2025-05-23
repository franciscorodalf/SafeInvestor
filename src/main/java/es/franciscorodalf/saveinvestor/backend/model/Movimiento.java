package es.franciscorodalf.saveinvestor.backend.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Clase para representar movimientos en la interfaz gráfica.
 */
public class Movimiento {

    public enum TipoMovimiento {
        INGRESO, GASTO;

        @Override
        public String toString() {
            return this == INGRESO ? "Ingreso" : "Gasto";
        }
    }

    private final ObjectProperty<LocalDate> fecha;
    private final ObjectProperty<TipoMovimiento> tipo;
    private final StringProperty concepto;
    private final ObjectProperty<Double> cantidad;
    private final IntegerProperty tareaId; // Nuevo campo para almacenar el ID de la tarea

    public Movimiento(LocalDate fecha, TipoMovimiento tipo, String concepto, Double cantidad, Integer tareaId) {
        this.fecha = new SimpleObjectProperty<>(fecha);
        this.tipo = new SimpleObjectProperty<>(tipo);
        this.concepto = new SimpleStringProperty(concepto);
        this.cantidad = new SimpleObjectProperty<>(cantidad);
        this.tareaId = new SimpleIntegerProperty(tareaId);
    }

    /**
     * Convierte una Tarea a un Movimiento para mostrarla en la interfaz
     */
    public static Movimiento fromTarea(tarea t) {
        TipoMovimiento tipo = t.getEstado().equals(tarea.ESTADO_INGRESO) ? TipoMovimiento.INGRESO
                : TipoMovimiento.GASTO;

        LocalDate fechaLocal = t.getFecha().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return new Movimiento(fechaLocal, tipo, t.getConcepto(), t.getCantidad(), t.getId());
    }

    public ObjectProperty<LocalDate> fechaProperty() {
        return fecha;
    }

    public LocalDate getFecha() {
        return fecha.get();
    }

    public ObjectProperty<TipoMovimiento> tipoProperty() {
        return tipo;
    }

    public TipoMovimiento getTipo() {
        return tipo.get();
    }

    public StringProperty conceptoProperty() {
        return concepto;
    }

    public String getConcepto() {
        return concepto.get();
    }

    public ObjectProperty<Double> cantidadProperty() {
        return cantidad;
    }

    public Double getCantidad() {
        return cantidad.get();
    }
    
    public IntegerProperty tareaIdProperty() {
        return tareaId;
    }
    
    public Integer getTareaId() {
        return tareaId.get();
    }
}
