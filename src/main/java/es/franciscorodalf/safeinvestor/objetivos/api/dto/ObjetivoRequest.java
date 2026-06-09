package es.franciscorodalf.safeinvestor.objetivos.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ObjetivoRequest(
    @NotBlank @Size(min = 1, max = 100) String nombre,
    @NotNull @DecimalMin(value = "0.01") BigDecimal importeObjetivo,
    LocalDate fechaLimite,
    @Pattern(regexp = "#[0-9A-Fa-f]{6}") String color
) {}
