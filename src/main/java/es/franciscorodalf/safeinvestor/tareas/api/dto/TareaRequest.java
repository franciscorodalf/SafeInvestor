package es.franciscorodalf.safeinvestor.tareas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TareaRequest(
    @NotBlank @Size(min = 1, max = 150) String titulo,
    @Size(max = 500) String descripcion,
    LocalDate fechaVencimiento
) {}
