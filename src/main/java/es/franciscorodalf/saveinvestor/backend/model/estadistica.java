package es.franciscorodalf.saveinvestor.backend.model;

/**
 * Clase que representa las estadísticas de un usuario
 */
public class estadistica {
    private Integer id;
    private Double totalIngreso;
    private Double totalGasto;
    private Integer usuarioId;
    
    public estadistica() {
        this.totalIngreso = 0.0;
        this.totalGasto = 0.0;
    }
    
    public estadistica(Double totalIngreso, Double totalGasto, Integer usuarioId) {
        this.totalIngreso = totalIngreso != null ? totalIngreso : 0.0;
        this.totalGasto = totalGasto != null ? totalGasto : 0.0;
        this.usuarioId = usuarioId;
    }

    /**
     * Calcula el balance (ingresos - gastos)
     * @return El balance del usuario
     */
    public Double getBalance() {
        return totalIngreso - totalGasto;
    }
    
    /**
     * Verifica si el balance es positivo
     * @return true si hay más ingresos que gastos
     */
    public boolean isBalancePositivo() {
        return getBalance() > 0;
    }
    
    // Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTotalIngreso() {
        return totalIngreso != null ? totalIngreso : 0.0;
    }

    public void setTotalIngreso(Double totalIngreso) {
        this.totalIngreso = totalIngreso != null ? totalIngreso : 0.0;
    }

    public Double getTotalGasto() {
        return totalGasto != null ? totalGasto : 0.0;
    }

    public void setTotalGasto(Double totalGasto) {
        this.totalGasto = totalGasto != null ? totalGasto : 0.0;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return "Estadistica [id=" + id + ", totalIngreso=" + totalIngreso + ", totalGasto=" + totalGasto
                + ", balance=" + getBalance() + ", usuarioId=" + usuarioId + "]";
    }
}
