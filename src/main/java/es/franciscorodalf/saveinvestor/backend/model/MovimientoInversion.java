package es.franciscorodalf.saveinvestor.backend.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class MovimientoInversion {
    private Integer id;
    private Integer cuentaId;
    private LocalDateTime fecha;
    private Double aporte;
    private Double retiro;
    private Double valorTotal;
    private Double rentabilidad;
    private String notas;

    public MovimientoInversion() {
    }

    public MovimientoInversion(Integer cuentaId, LocalDateTime fecha, Double valorTotal) {
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.valorTotal = valorTotal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Integer cuentaId) {
        this.cuentaId = cuentaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getAporte() {
        return aporte;
    }

    public void setAporte(Double aporte) {
        this.aporte = aporte;
    }

    public Double getRetiro() {
        return retiro;
    }

    public void setRetiro(Double retiro) {
        this.retiro = retiro;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Double getRentabilidad() {
        return rentabilidad;
    }

    public void setRentabilidad(Double rentabilidad) {
        this.rentabilidad = rentabilidad;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovimientoInversion)) {
            return false;
        }
        MovimientoInversion that = (MovimientoInversion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MovimientoInversion{" +
                "id=" + id +
                ", cuentaId=" + cuentaId +
                ", fecha=" + fecha +
                ", aporte=" + aporte +
                ", retiro=" + retiro +
                ", valorTotal=" + valorTotal +
                ", rentabilidad=" + rentabilidad +
                ", notas='" + notas + '\'' +
                '}';
    }
}
