package es.franciscorodalf.safeinvestor.objetivos.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AportarRequest(
    @NotNull @DecimalMin(value = "0.01") BigDecimal importe
) {}
