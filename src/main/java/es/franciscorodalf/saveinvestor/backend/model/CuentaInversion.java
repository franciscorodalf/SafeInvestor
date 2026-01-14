package es.franciscorodalf.saveinvestor.backend.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class CuentaInversion {

    public enum TipoActivo {
        AHORRO,
        FONDO,
        CRYPTO;

        public static TipoActivo fromDatabaseValue(String value) {
            if (value == null) {
                throw new IllegalArgumentException("El tipo de activo no puede ser nulo");
            }
            return TipoActivo.valueOf(value.toUpperCase());
        }

        public String toDatabaseValue() {
            return name();
        }
    }

    private Integer id;
    private Integer usuarioId;
    private String nombre;
    private TipoActivo tipoActivo;
    private String moneda;
    private Double valorInicial;
    private Double valorActual;
    private LocalDateTime fechaCreacion;
    private String descripcion;

    public CuentaInversion() {
    }

    public CuentaInversion(Integer usuarioId, String nombre, TipoActivo tipoActivo) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.tipoActivo = tipoActivo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoActivo getTipoActivo() {
        return tipoActivo;
    }

    public void setTipoActivo(TipoActivo tipoActivo) {
        this.tipoActivo = tipoActivo;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Double getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(Double valorInicial) {
        this.valorInicial = valorInicial;
    }

    public Double getValorActual() {
        return valorActual;
    }

    public void setValorActual(Double valorActual) {
        this.valorActual = valorActual;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CuentaInversion)) {
            return false;
        }
        CuentaInversion that = (CuentaInversion) o;
        return Objects.equals(id, that.id) && Objects.equals(usuarioId, that.usuarioId) && Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, nombre);
    }

    @Override
    public String toString() {
        return "CuentaInversion{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", nombre='" + nombre + '\'' +
                ", tipoActivo=" + tipoActivo +
                ", moneda='" + moneda + '\'' +
                ", valorInicial=" + valorInicial +
                ", valorActual=" + valorActual +
                ", fechaCreacion=" + fechaCreacion +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
