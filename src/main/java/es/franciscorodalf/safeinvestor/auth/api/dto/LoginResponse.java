package es.franciscorodalf.safeinvestor.auth.api.dto;

public record LoginResponse(String token, String email, String nombre) {}
