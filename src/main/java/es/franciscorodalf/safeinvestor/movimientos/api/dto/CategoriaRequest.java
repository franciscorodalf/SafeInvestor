package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank @Size(min = 1, max = 50) String nombre,
    @Pattern(regexp = "#[0-9A-Fa-f]{6}") String color,
    @Size(max = 50) String icono
) {}
