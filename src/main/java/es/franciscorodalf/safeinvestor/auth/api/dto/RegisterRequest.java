package es.franciscorodalf.safeinvestor.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 1, max = 100) String nombre,
    @NotBlank @Size(min = 8, max = 72) String password
) {}
